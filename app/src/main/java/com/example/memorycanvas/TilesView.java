package com.example.memorycanvas;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Random;

class Card {
    Paint p = new Paint();

    public Card(float x, float y, float width, float height, int color) {
        this.color = color;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    int color, backColor = Color.DKGRAY;
    boolean isOpen = false; // цвет карты
    float x, y, width, height;
    public void draw(Canvas c) {
        // нарисовать карту в виде цветного прямоугольника
        if (isOpen) {
            p.setColor(color);
        } else p.setColor(backColor);
        c.drawRect(x,y, x+width, y+height, p);
    }
    public boolean flip (float touch_x, float touch_y) {
        if (touch_x >= x && touch_x <= x + width && touch_y >= y && touch_y <= y + height) {
            isOpen = ! isOpen;
            return true;
        } else return false;
    }
}

public class TilesView extends View {
    // пауза для запоминания карт
    final int PAUSE_LENGTH = 1; // в секундах
    boolean isOnPauseNow = false;

    // число открытых карт
    int openedCard = 0;
    // количество карт
    int count_cards_row = 4;
    int count_cards_col = 2;
    // массив цветов
    ArrayList<Integer> colors;
    // использованные цвета
    SparseIntArray used_color;
    // количество цветов
    int amountColors;
    boolean gameOn = false;

    boolean check = true;
    Random random = new Random();

    ArrayList<Card> cards = new ArrayList<>();
    ArrayList<Card> openCards = new ArrayList<>();

    int width, height; // ширина и высота канвы

    public TilesView(Context context) {
        super(context);
    }

    public TilesView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        // 1) заполнить массив tiles случайными цветами
        // сгенерировать поле 2*n карт, при этом
        // должно быть ровно n пар карт разных цветов
        colors = new ArrayList<>();
        used_color = new SparseIntArray();
        colors.add(Color.MAGENTA);
        colors.add(Color.RED);
        colors.add(Color.GREEN);
        colors.add(Color.YELLOW);
//        colors.add(Color.WHITE);
//        colors.add(Color.GRAY);
//        colors.add(Color.BLACK);
//        colors.add(Color.CYAN);
        amountColors = colors.size();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        width = getWidth();
        height = getHeight();
        // инициализация карт
        if(check){
            initCards();
            check = false;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 2) отрисовка плиток
        // задать цвет можно, используя кисть
        Paint p = new Paint();
        if(gameOn) {
            for (Card c : cards) {
                c.draw(canvas);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 3) получить координаты касания
        int x = (int) event.getX();
        int y = (int) event.getY();
        // 4) определить тип события
        if (event.getAction() == MotionEvent.ACTION_DOWN && !isOnPauseNow)
        {
            // палец коснулся экрана
            for (Card c: cards) {

                if (openedCard == 0) {
                    if (c.flip(x, y)) {
                        openCards.add(c);
                        Log.d("mytag", "1 card = " + c.color + " size = " + openCards.size());
                        Log.d("mytag", "card flipped: " + openedCard);
                        openedCard ++;
                        invalidate();
                        return true;
                    }
                }

                if (openedCard == 1) {
                    // перевернуть карту с задержкой
                    if (c.flip(x, y)) {
                        openedCard ++;
                        openCards.add(c);
                        // 1) если открылис карты одинакового цвета, удалить их из списка
                        // например написать функцию, checkOpenCardsEqual

                        // 2) проверить, остались ли ещё карты
                        // иначе сообщить об окончании игры
                        invalidate();
                        // если карты открыты разного цвета - запустить задержку
                        PauseTask task = new PauseTask();
                        task.execute(PAUSE_LENGTH);
                        isOnPauseNow = true;
                        return true;
                    }
                }
            }
        }
         // заставляет экран перерисоваться
        return true;
    }

    public void initCards(){
        used_color.clear();
        for (int i = 0; i < amountColors; i++) {
            used_color.put(colors.get(i), 0);
        }
        float card_width = width / count_cards_col;
        float card_height = height / count_cards_row;
        int padding = 10;
        float width_new = card_width - (padding * 2);
        float height_new  = card_height - (padding * 2);
        for(int i = 1; i < count_cards_row + 1; i++) {
            for (int j = 1; j < count_cards_col + 1; j++) {
                cards.add(new Card(padding + (card_width * j - card_width), card_height * i - card_height + padding, width_new, height_new, getColor()));
            }
        }
    }

    public int getColor(){
        int color_number = random.nextInt(amountColors);
        int color = colors.get(color_number);
        while(used_color.get(color) == 2) {
            color_number = random.nextInt(amountColors);
            color = colors.get(color_number);
        }
        used_color.put(color, used_color.get(color) + 1);
        return color;
    }

    public void newGame() {
        // запуск новой игры
        cards.clear();
        initCards();
        invalidate();
    }

    public void turnOnGame(){
        gameOn = true;
        invalidate();
    }

    public void gameOver(){
        Toast.makeText(getContext(), "You win", Toast.LENGTH_SHORT).show();
    }

    class PauseTask extends AsyncTask<Integer, Void, Void> {
        @Override
        protected Void doInBackground(Integer... integers) {
            Log.d("mytag", "Pause started");
            try {
                Thread.sleep(integers[0] * 1000); // передаём число секунд ожидания
            } catch (InterruptedException e) {}
            Log.d("mytag", "Pause finished");
            return null;
        }

        // после паузы, перевернуть все карты обратно


        @Override
        protected void onPostExecute(Void aVoid) {
            for (Card c: cards) {
                if (c.isOpen) {
                    c.isOpen = false;
                }
            }
            if(openCards.get(0).color == openCards.get(1).color){
                cards.remove(openCards.get(0));
                cards.remove(openCards.get(1));
            }
            openedCard = 0;
            isOnPauseNow = false;
            openCards.clear();
            if(cards.size() == 0){
                gameOver();
            }
            invalidate();
        }
    }
}
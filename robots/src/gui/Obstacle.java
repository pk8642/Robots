package gui;

import javafx.geometry.Bounds;
import javafx.scene.shape.Line;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.Random;

public class Obstacle extends Rectangle implements Serializable {

    private Point leftUp;
    private Point rightUp;
    private Point leftDown;
    private Point rightDown;

    private int x;
    private int y;
    private int width;
    private int height;

    private Point anotherLeftUp;
    private Point anotherLeftDown;
    private Point anotherRightUp;
    private Point anotherRightDown;
    private Obstacle(){}
    public Obstacle(Point p) {
       leftUp = new Point(p.x-25,p.y-25);
       rightUp = new Point(p.x+25, p.y-25);
       leftDown = new Point(p.x-25, p.y+25);
       rightDown = new Point(p.x+25,p.y+25);

       super.x = leftUp.x;
       super.y=leftUp.y;
       super.width=50;
       super.height=50;

       anotherLeftUp = new Point(this.leftUp.x-2, this.leftUp.y-2);//точки графа(использовал немного отступающие в поле точки от вершины)
       anotherRightUp = new Point(this.rightUp.x+2, this.rightUp.y-2);
       anotherLeftDown = new Point(this.leftDown.x-2, this.leftDown.y+2);
       anotherRightDown = new Point(this.rightDown.x+2, this.rightDown.y+2);
    }

    private Color randomColor(){//рандомная заливка
        Color[] colors = new Color[]{Color.WHITE,Color.GREEN,Color.MAGENTA,Color.BLUE,Color.CYAN,Color.LIGHT_GRAY,Color.ORANGE,Color.RED,Color.YELLOW};
        Random randomColour = new Random();
        int color = randomColour.nextInt(colors.length-1 +1);
        return colors[color];
    }

    void paint(Graphics g){//рисовка робота
        g.setColor(randomColor());
        g.fillRect(this.leftUp.x+1,this.leftUp.y+1,49,49);
        g.setColor(Color.BLACK);
        g.drawRect(this.leftUp.x,this.leftUp.y,50,50);
    }

    boolean intersect(Line line){//проверка на пересечение
        return this.intersectsLine(line.getStartX(),line.getStartY(),line.getEndX(),line.getEndY());
    }

    public Point getAnother(Point p){//получение точки графа по вершине фигуры
        if(p.equals(leftDown))return getAnotherLeftDown();
        else if(p.equals(leftUp))
            return getAnotherLeftUp();
        else if(p.equals(rightDown))
            return getAnotherRightDown();
        else return getAnotherRightUp();
    }

    public Point getAnotherLeftUp(){return anotherLeftUp;}

    public Point getAnotherLeftDown(){return anotherLeftDown;}

    public Point getAnotherRightUp(){return anotherRightUp;}

    public Point getAnotherRightDown(){return anotherRightDown;}


    public Point getLeftUp() {
        return this.leftUp;
    }

    public Point getRightUp() {
        return this.rightUp;
    }

    public Point getLeftDown() {
        return this.leftDown;
    }

    public Point getRightDown() {
        return this.rightDown;
    }
}

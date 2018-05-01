package gui;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.Random;

public class Obstacle implements Serializable {

    private Point leftUp;
    private Point rightUp;
    private Point leftDown;
    private Point rightDown;

    private Obstacle(){}

    public Obstacle(Point p) {
        leftUp = new Point(p.x-25,p.y-25);
        leftDown = new Point(p.x+25, p.y-25);
        rightUp = new Point(p.x-25, p.y+25);
        rightDown = new Point(p.x+25,p.y+25);
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

    boolean hasInBorder(Point p){
        return  (p.x>this.leftUp.x&&p.y>this.leftUp.y&&
                p.x<this.rightDown.x&&p.y<this.rightDown.y);
    }

    public Point getLeftUp() {
        return leftUp;
    }

    public Point getRightUp() {
        return rightUp;
    }

    public Point getLeftDown() {
        return leftDown;
    }

    public Point getRightDown() {
        return rightDown;
    }
}

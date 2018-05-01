package gui;

import javafx.beans.Observable;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Observer;
import java.util.Stack;
import java.util.Vector;

public class RobotMove extends java.util.Observable {
    ArrayList<Obstacle> obstacles = new ArrayList<>();
    ArrayList<Observer> observable = new ArrayList<>();
    protected volatile double m_robotPositionX;
    protected volatile double m_robotPositionY;
    protected volatile double m_robotDirection;

    protected volatile int m_targetPositionX;
    protected volatile int m_targetPositionY;

    private static final double maxVelocity = 0.1;


    public RobotMove(){
        m_robotPositionX=100;
        m_robotPositionY=100;
        m_robotDirection=0;
        m_targetPositionX=150;
        m_targetPositionY=100;
    }

    protected void setTargetPosition(Point p)
    {
        m_targetPositionX = p.x;
        m_targetPositionY = p.y;
    }

    protected Point getTargetPosition(){
        return new Point(m_targetPositionX,m_targetPositionY);
    }


    private static double distance(double x1, double y1, double x2, double y2)
    {
        double diffX = x1 - x2;
        double diffY = y1 - y2;
        return Math.sqrt(diffX * diffX + diffY * diffY);
    }

    private static double angleTo(double fromX, double fromY, double toX, double toY)
    {
        double diffX = toX - fromX;
        double diffY = toY - fromY;

        return asNormalizedRadians(Math.atan2(diffY,diffX));
    }

    protected void onModelUpdateEvent()
    {
        double distance = distance(m_targetPositionX, m_targetPositionY,
                m_robotPositionX, m_robotPositionY);
        if (distance <= 0.5)
        {
            return;
        }
        double velocity = maxVelocity;
        double newDirection = angleTo(m_robotPositionX,m_robotPositionY,m_targetPositionX,m_targetPositionY);
        m_robotDirection = newDirection;
        moveRobot(velocity, 10);
        setChanged();
        notifyObservers();
    }
    private void moveRobot(double velocity, double duration)//шаг робота
    {
        double newX = m_robotPositionX + velocity * duration * Math.cos(m_robotDirection);

        double newY = m_robotPositionY + velocity * duration * Math.sin(m_robotDirection);
        m_robotPositionX = newX;
        m_robotPositionY = newY;
    }

    /*private Point algDijkstra(){//сделать алгоритм Дейкстры
        ArrayList<Point> track = new ArrayList<>();
        ArrayList<Point> verticses = new ArrayList<>();
        Point start = new Point((int) Math.round(m_robotPositionX),(int)Math.round(m_robotPositionY));
        Point finish = new Point(m_targetPositionX,m_targetPositionY);

    }

    private int[][] setMatrix(){//инициализировать матрицу
        int[][] matrix = new int[][]{};
        for (int i = 0; i<observable.size();i++){
            for (int j=0;j<observable.size();j++){

            }
        }
    }*/

    //сделать метод для проверки пересечения отрезков


    private static double asNormalizedRadians(double angle)
    {
        while (angle < 0)
        {
            angle += 2*Math.PI;
        }
        while (angle >= 2*Math.PI)
        {
            angle -= 2*Math.PI;
        }
        return angle;
    }


    public void notifyObservers(){//обновление данных наблюдателей
        for(Observer o: observable){
            o.update(this,null);
        }
    }

}
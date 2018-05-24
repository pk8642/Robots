package gui;

import algs.AlgDijkstra;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import java.awt.*;
import java.awt.geom.Line2D;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class RobotMove extends java.util.Observable {

    private volatile Map<Point, ArrayList<Point>> map = new HashMap<>();
    volatile ArrayList<Obstacle> obstacles = new ArrayList<>();
    volatile ArrayList<Observer> observable = new ArrayList<>();
    volatile double m_robotPositionX;
    volatile double m_robotPositionY;
    volatile double m_robotDirection;

    volatile int m_targetPositionX;
    volatile int m_targetPositionY;

    private static final double maxVelocity = 0.1;

    Class cls= AlgDijkstra.class;
    Method method;
    {
        try {
            method = cls.getMethod("alg",Point.class,Point.class,HashMap.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }


    public RobotMove() {
        m_robotPositionX = 100;
        m_robotPositionY = 100;
        m_robotDirection = 0;
        m_targetPositionX = 150;
        m_targetPositionY = 100;
    }

    protected void setTargetPosition(Point p) {
        m_targetPositionX = p.x;
        m_targetPositionY = p.y;
    }

    protected Point getTargetPosition() {
        return new Point(m_targetPositionX, m_targetPositionY);
    }


    private static double distance(double x1, double y1, double x2, double y2) {
        double diffX = x1 - x2;
        double diffY = y1 - y2;
        return Math.sqrt(diffX * diffX + diffY * diffY);
    }

    private static double angleTo(double fromX, double fromY, double toX, double toY) {
        double diffX = toX - fromX;
        double diffY = toY - fromY;

        return asNormalizedRadians(Math.atan2(diffY, diffX));
    }

    protected void onModelUpdateEvent() {
        double distance = distance(m_targetPositionX, m_targetPositionY,
                m_robotPositionX, m_robotPositionY);
        if (distance <= 0.5) {
            return;
        }
        double velocity = maxVelocity;

        Point finish = new Point(m_targetPositionX, m_targetPositionY);//конечная точка
        Point start = new Point((int) Math.round(m_robotPositionX), (int) Math.round(m_robotPositionY));//стартовая точка
        Point newTarget =  makeGraph(start,finish);
        try {
            method = cls.getDeclaredMethod("alg",Point.class,Point.class,HashMap.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        try {
            if (newTarget.equals(new Point(-1,-1))) {
                Object res = method.invoke(cls.newInstance(), start, finish, map);
                newTarget = (Point) res;
            }

            if (!newTarget.equals(new Point((int)Math.round(m_robotPositionX),(int)Math.round(m_robotPositionY)))) {
                m_robotDirection = angleTo(m_robotPositionX, m_robotPositionY, newTarget.x, newTarget.y);
                moveRobot(velocity, 10);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();

        }
        catch (InstantiationException e) {
            e.printStackTrace();
            System.out.println("Ldf;ls");
        }catch (InvocationTargetException e) {
            e.getCause().printStackTrace();
            System.out.println("Vfvre ndj. t,fk");
        }
        setChanged();
        notifyObservers();
    }

    private Point makeGraph(Point start, Point finish) {
        map = new HashMap<>();//коллекция ключей-вершин и значений-списков вершин, до которых они могут дойти

        ArrayList<Point> vertices = new ArrayList<>();//множество вершин

        vertices.add(finish);
        for (int i = 0; i < obstacles.size(); i++) {
            ArrayList<Point> list = new ArrayList<>();
            Obstacle o = obstacles.get(i);
            list.add(o.getLeftUp());//добавляю все точки препятствий
            list.add(o.getLeftDown());
            list.add(o.getRightDown());
            list.add(o.getRightUp());
            for (Point vertex : list) {//для каждой точки препятствия
                Point anotherVertex = o.getAnother(vertex);
                if (distance(m_robotPositionX, m_robotPositionY, vertex.x, vertex.y) <= 2) {//чтобы робот случайно не зацепил угол фигуры
                    return anotherVertex;
                }
                vertices.add(anotherVertex);
                for (int j = i; j < obstacles.size(); j++) {//построение графа со всеми вершинами препятствий
                    Obstacle o2 = obstacles.get(j);
                    mappingLines(anotherVertex, o2.getAnotherLeftDown());
                    mappingLines(anotherVertex, o2.getAnotherRightDown());
                    mappingLines(anotherVertex, o2.getAnotherRightUp());
                    mappingLines(anotherVertex, o2.getAnotherLeftUp());
                }
                if (o.contains(start) || o.contains(finish))//если стартовая точка или финишная попали в препятвие, то остановить робота
                    return start;
                mappingLines(start, anotherVertex);//достроение графа точкой старта и финиша
                mappingLines(anotherVertex, finish);
            }
        }
        if (obstacles.isEmpty()) {
            return finish;
        }
        mappingLines(start, finish);
        return new Point(-1,-1);
    }

    private void mappingLines(Point p1, Point p2) {
        boolean intersection = false;
        if (p1.equals(p2))
            return;
        for (int i = 0; i < obstacles.size(); i++) {
            Obstacle obstacle = obstacles.get(i);
            if (obstacle.intersect(new Line2D.Double(p1.x, p1.y, p2.x, p2.y))) {
                intersection = true;
                break;
            }
        }
        if (!intersection) {
            if (!map.containsKey(p1))
                map.put(p1, new ArrayList<>());
            ArrayList<Point> list = map.get(p1);
            list.add(p2);
            if (!map.containsKey(p2))
                map.put(p2, new ArrayList<>());
            list = map.get(p2);
            list.add(p1);
        }
    }

    private void moveRobot(double velocity, double duration)//шаг робота
    {
        double newX = m_robotPositionX + velocity * duration * Math.cos(m_robotDirection);
        double newY = m_robotPositionY + velocity * duration * Math.sin(m_robotDirection);
        m_robotPositionX = newX;
        m_robotPositionY = newY;
    }



    private static double asNormalizedRadians(double angle) {
        while (angle < 0) {
            angle += 2 * Math.PI;
        }
        while (angle >= 2 * Math.PI) {
            angle -= 2 * Math.PI;
        }
        return angle;
    }

    public void notifyObservers() {//обновление данных наблюдателей
        for (Observer o : observable) {
            o.update(this, null);
        }
    }


}
package gui;

import java.awt.*;
import java.awt.geom.Line2D;
import java.util.*;

public class RobotMove extends java.util.Observable {

    private volatile Map<Point, ArrayList<Point>> map = new HashMap<>();
    private volatile Map<Point,Double> distance;
    volatile ArrayList<Obstacle> obstacles = new ArrayList<>();
    volatile ArrayList<Observer> observable = new ArrayList<>();
    volatile double m_robotPositionX;
    volatile double m_robotPositionY;
    volatile double m_robotDirection;

    volatile int m_targetPositionX;
    volatile int m_targetPositionY;

    private static final double maxVelocity = 0.1;


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


        Point newTarget = algDijkstra();//новая цель - ближайшая точка, почситаная по алгоритму Дейкстры
        if (!newTarget.equals(new Point((int)Math.round(m_robotPositionX),(int)Math.round(m_robotPositionY)))) {
            //если полученная точка равна точке start из метода algDijkstra, то не двигать
            m_robotDirection = angleTo(m_robotPositionX, m_robotPositionY, newTarget.x, newTarget.y);
            moveRobot(velocity, 10);
        }

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

    private Point algDijkstra() {
        map = new HashMap<>();//коллекция ключей-вершин и значений-списков вершин, до которых они могут дойти
        distance = new HashMap<>();//дистанция от стартовой точки до всех
        HashMap<Point,Point> prev = new HashMap<>();//список предыдущих, по которому восстановится маршрут
        ArrayList<Point> track = new ArrayList<>();//маршрут
        ArrayList<Point> vertices = new ArrayList<>();//множество вершин
        Point finish = new Point(m_targetPositionX, m_targetPositionY);//конечная точка
        Point start = new Point((int) Math.round(m_robotPositionX), (int) Math.round(m_robotPositionY));//стартовая точка
        vertices.add(finish);
        for (int i =0;i<obstacles.size();i++) {
            ArrayList<Point> list = new ArrayList<>();
            Obstacle o=obstacles.get(i);
            list.add(o.getLeftUp());//добавляю все точки препятствий
            list.add(o.getLeftDown());
            list.add(o.getRightDown());
            list.add(o.getRightUp());
            for (Point vertex : list) {//для каждой точки препятствия
                Point anotherVertex = o.getAnother(vertex);
                if (distance(m_robotPositionX, m_robotPositionY, vertex.x, vertex.y) <= 2)//чтобы робот случайно не зацепил угол фигуры
                    return anotherVertex;
                vertices.add(anotherVertex);
                for (int j=i;j<obstacles.size();j++) {//построение графа со всеми вершинами препятствий
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
        for (Point p : vertices) {
            if (map.get(start).contains(p)) {//инициализация
                map.get(p).remove(start);
                distance.put(p,distance(m_robotPositionX, m_robotPositionY, p.x, p.y));
                prev.put(p,start);//старт - перед p
            }
            else distance.put(p,1000000.0);
        }
        map.remove(finish);
        int n = vertices.size();//количество вершин
        for (int k = 1; k < n; k++) {//количество итераций
            Point w = minV(vertices);//беру точку с наименьшим расстоянием от начала
            vertices.remove(w);//удаляю из множества вершин
            if (!map.containsKey(w))
                continue;
            for (Point v : map.get(w)) {//высчитывание кратчайшего расстояния
                if (distance.containsKey(v)) {
                    if (distance.get(w) + distance(w.x, w.y, v.x, v.y) < distance.get(v)) {
                        distance.put(v, distance.get(w) + distance(w.x, w.y, v.x, v.y));
                        prev.put(v, w);
                    }
                } else {
                    distance.put(v, distance.get(w) + distance(w.x, w.y, v.x, v.y));
                    prev.put(v, w);
                }

            }
        }
        try {//возврат ближайшей точки
            Point t = finish;
            track.add(t);
            while (!(t.equals(start))) {
                t = prev.get(t);
                track.add(t);
            }
            if (track.size() == 2)//если вершины 2, то это старт и финиш
                return finish;
            else if (track.size() > 2)//если больше, то сразу после стартовой ближайшая
                return track.get(track.size() - 2);//стартовая записана последней
            else return start;//необъяснимая ситуация, вернуть стартовую точку
        } catch (NullPointerException e) {
            if (track.size() > 2)
                return track.get(1);
            else return start;
        }
    }


    private void mappingLines(Point p1, Point p2) {
        boolean intersection=false;
        if (p1.equals(p2))
            return;
        for (int i = 0; i<obstacles.size();i++) {
            Obstacle obstacle = obstacles.get(i);
            if (obstacle.intersect(new Line2D.Double(p1.x, p1.y, p2.x, p2.y))) {
                intersection = true;
                break;
            }
        }
        if(!intersection) {
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

    private Point minV(ArrayList<Point> list) {//наименьшее значение из данных
        Point min = list.get(0);
        for (Point p : list) {
            if (distance.containsKey(p)&&distance.containsKey(min))
                if (distance.get(p) < distance.get(min))
                    min = p;
            }
        return min;
    }
}
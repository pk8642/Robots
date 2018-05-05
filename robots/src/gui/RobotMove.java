package gui;

import javafx.beans.Observable;
import javafx.scene.shape.Line;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;

public class RobotMove extends java.util.Observable {

    volatile Map<Point,ArrayList<Point>> map = new HashMap<>();

    private volatile double[][] distance;
    volatile ArrayList<Obstacle> obstacles = new ArrayList<>();
    volatile ArrayList<Observer> observable = new ArrayList<>();
    protected volatile double m_robotPositionX;
    protected volatile double m_robotPositionY;
    protected volatile double m_robotDirection;

    protected volatile int m_targetPositionX;
    protected volatile int m_targetPositionY;

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
        Point newTarget = algDijkstra();
        m_robotDirection = angleTo(m_robotPositionX, m_robotPositionY, newTarget.x, newTarget.y);
        moveRobot(velocity, 10);
        setChanged();
        notifyObservers();
    }

    private void moveRobot(double velocity, double duration)//шаг робота
    {
        double newX = m_robotPositionX + velocity * duration * Math.cos(m_robotDirection);

        double newY = m_robotPositionY + velocity * duration * Math.sin(m_robotDirection);
        m_robotPositionX =  newX;
        m_robotPositionY =  newY;
    }

    private Point algDijkstra() {
        map=new HashMap<>();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        distance=new  double[screenSize.height][screenSize.width];
        Point[][] prev = new Point[screenSize.height][screenSize.width];
        ArrayList<Point> track = new ArrayList<>();
        ArrayList<Point> verticses = new ArrayList<>();//множество вершин
        Point finish = new Point(m_targetPositionX, m_targetPositionY);
        Point start = new Point((int) m_robotPositionX, (int) m_robotPositionY);
        verticses.add(start);
        verticses.add(finish);
        for (Obstacle o : obstacles) {
            verticses.add(o.getAnotherLeftUp());//добавляю все точки препятствий
            verticses.add(o.getAnotherLeftDown());
            verticses.add(o.getAnotherRightDown());
            verticses.add(o.getAnotherRightUp());
            if(o.contains(start)||o.contains(finish)){
                finish = o.getAnotherRightUp();//пусть идет к ближайшей точке
                return finish;
            }
        }
        if (obstacles.isEmpty()) {
            return finish;
        }

        setDistanceArray(start, finish);
        for (Point p : verticses) {
            if (map.get(start).contains(p)||p.equals(start)) {//инициализация
                distance[p.x][p.y] = distance(start.x, start.y, p.x, p.y);
                prev[p.x][p.y]=start;
            }
            else distance[p.x][p.y]=1000000;
        }
        verticses.remove(start);
        int n = verticses.size();//количество вершин
        for (int k = 1; k < n; k++) {//количество итераций
            Point w = minV(verticses);//беру точку с наименьшим расстоянием от начала
            verticses.remove(w);//удаляю из множества вершин
            if(!map.containsKey(w))
                continue;
                for (Point v : map.get(w)) {
                    if (distance[w.x][w.y] + distance(w.x, w.y, v.x, v.y) < distance[v.x][v.y]) {
                        distance[v.x][v.y] = distance[w.x][w.y] + distance(w.x, w.y, v.x, v.y);
                        prev[v.x][v.y] = w;
                    }
                }

        }
        Point t = finish;
        track.add(t);
        while (!(t.equals(start))) {
            t = prev[t.x][t.y];
            track.add(t);
        }
        Collections.reverse(track);
        if (track.size()==2)
            return finish;
        else if(track.size()>2)
            track.remove(track.get(0));
        finish = track.get(0);
        return finish;

    }


    private void setDistanceArray(Point start, Point finish) {
        boolean intersection = false;
        Line line = new Line(start.x, start.y, finish.x, finish.y);
        for (Obstacle o : obstacles) {
            if (o.contains(start)||o.contains(finish))
                return;
            else if (o.intersect(line)) {
                intersection = true;
                ArrayList<Double> distances=new ArrayList<>();
                ArrayList<Point> points= new ArrayList<>();

                distances.add(distance(start.x,start.y,o.getLeftDown().x,o.getLeftDown().y));
                points.add(o.getLeftDown());
                distances.add(distance(start.x, start.y, o.getLeftUp().x, o.getLeftUp().y));
                points.add(o.getLeftUp());
                distances.add(distance(start.x, start.y, o.getRightDown().x, o.getRightDown().y));
                points.add(o.getRightDown());
                distances.add(distance(start.x, start.y, o.getRightUp().x, o.getRightUp().y));
                points.add(o.getRightUp());

                sort(points, distances,0,3);

                Point firstNear = o.getAnother(points.get(0));
                Point secondNear = o.getAnother(points.get(1));
                Point thirdNear = o.getAnother(points.get(2));
                Point fourthNear = o.getAnother(points.get(3));

                //отсортировать по близости к начальной точке
                if(!o.intersect(new Line(start.x,start.y,firstNear.x,firstNear.y)))
                    setDistanceArray(start, firstNear);
                if (!o.intersect(new Line(start.x,start.y,secondNear.x,secondNear.y)))
                    setDistanceArray(start, secondNear);
                if (!o.intersect(new Line(thirdNear.x,thirdNear.y,finish.x,finish.y)))
                    setDistanceArray(thirdNear, finish);
                if (!o.intersect(new Line(fourthNear.x,fourthNear.y,finish.x,finish.y)))
                    setDistanceArray(fourthNear, finish);
                //разбить на 2 части, взяв точку деления как препятсвие
                setDistanceArray(firstNear, thirdNear);
                setDistanceArray(secondNear, fourthNear);
            }
        }
        if (!intersection) {
            if (!map.containsKey(start))
                map.put(start, new ArrayList<>());
            ArrayList<Point> list = map.get(start);
            list.add(finish);
            if (!map.containsKey(finish))
                map.put(finish, new ArrayList<>());
            list = map.get(finish);
            list.add(start);
        }
    }

        private static double asNormalizedRadians ( double angle)
        {
            while (angle < 0) {
                angle += 2 * Math.PI;
            }
            while (angle >= 2 * Math.PI) {
                angle -= 2 * Math.PI;
            }
            return angle;
        }


        public void notifyObservers () {//обновление данных наблюдателей
            for (Observer o : observable) {
                o.update(this, null);
            }
        }



    private Point minV(ArrayList<Point> list) {
        Point min= list.get(0);
        for (Point p : list) {
                if (distance[p.x][p.y] <distance[min.x][min.y])
                    min = p;
            }
        return min;
    }

    private void sort(ArrayList<Point> keys,ArrayList<Double> values, int left, int right) {//метод из сортировок(можно юзать свой)
        if (keys.isEmpty()) return;
        if (left >= right) return;

        int i = left;
        int j = right;
        int middle = (i + j) / 2;
        while (i < j) {//разделение на группу элементов, меньших выбранного и больших его
            double centre = values.get(middle);//выбранный элемент, находящийся изначально по центру
            while (values.get(i) <= centre && i < middle)//выбирается элемент слева, который меньше centre
                i++;
            while (values.get(j) >= centre && j > middle)//выбирается элемент справа, который больше centre
                j--;
            //вот здесь как раз играет на пользу неизменяемость порядка листа ключей и листа значений относительно мапы
            Collections.swap(values, i, j);//выбранные элементы в значениях меняются местами, образуя часть меньшую centre и большую его
            Collections.swap( keys, i, j);//выбранные элементы в ключах меняются местами
            if (i == middle)
                middle = j;
            else if (j == middle)
                middle = i;

        }
        sort(keys, values, left, middle);
        sort(keys, values, middle + 1, right);
    }
}
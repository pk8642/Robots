package gui;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JPanel;

public class GameVisualizer extends JPanel
{
    private final Timer m_timer = initTimer();
    private static Timer initTimer() 
    {
        Timer timer = new Timer("events generator", true);
        return timer;
    }

    RobotMove robot = new RobotMove();

    public GameVisualizer(){
        m_timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                onRedrawEvent();
            }
        }, 0, 50);
        m_timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                robot.onModelUpdateEvent();
            }
        }, 0, 10);
        addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e) {

                    if (e.getButton() == MouseEvent.BUTTON1) {
                        robot.setTargetPosition(e.getPoint());
                        repaint();
                    }
                    if (e.getButton() == MouseEvent.BUTTON3) {
                        robot.obstacles.add(new Obstacle(e.getPoint()));
                    }
                    if (e.getButton() == MouseEvent.BUTTON2) {
                        Iterator iterator = robot.obstacles.iterator();//чтобы избежать ConcurrentModificationException
                        while (iterator.hasNext()){
                            Obstacle o = (Obstacle) iterator.next();
                            if (o.contains(e.getPoint())) {
                                iterator.remove();
                            }
                        }
                    }

            }
        });
                setDoubleBuffered(true);
    }

    protected void onRedrawEvent()
    {
        EventQueue.invokeLater(this::repaint);
    }

    private static int round(double value)
    {
        return (int)(value + 0.5);
    }
    
    @Override
    public void paint(Graphics g)
    {
        super.paint(g);
        Graphics2D g2d = (Graphics2D)g; 
        drawRobot(g2d, round(robot.m_robotPositionX), round(robot.m_robotPositionY), robot.m_robotDirection);
        drawTarget(g2d, robot.m_targetPositionX, robot.m_targetPositionY);
        g.drawLine((int)robot.m_robotPositionX,(int)robot.m_robotPositionY,robot.m_targetPositionX,robot.m_targetPositionY);
        for(int i=0; i<robot.obstacles.size(); i++){
            robot.obstacles.get(i).paint(g2d);
        }
    }
    
    private static void fillOval(Graphics g, int centerX, int centerY, int diam1, int diam2)
    {
        g.fillOval(centerX - diam1 / 2, centerY - diam2 / 2, diam1, diam2);
    }
    
    private static void drawOval(Graphics g, int centerX, int centerY, int diam1, int diam2)
    {
        g.drawOval(centerX - diam1 / 2, centerY - diam2 / 2, diam1, diam2);
    }
    
    private void drawRobot(Graphics2D g, int x, int y, double direction)
    {
        int robotCenterX = round(robot.m_robotPositionX);
        int robotCenterY = round(robot.m_robotPositionY);
        AffineTransform t = AffineTransform.getRotateInstance(direction, robotCenterX, robotCenterY); 
        g.setTransform(t);
        g.setColor(Color.MAGENTA);
        fillOval(g, robotCenterX, robotCenterY, 30, 10);
        g.setColor(Color.BLACK);
        drawOval(g, robotCenterX, robotCenterY, 30, 10);
        g.setColor(Color.WHITE);
        fillOval(g, robotCenterX  + 10, robotCenterY, 5, 5);
        g.setColor(Color.BLACK);
        drawOval(g, robotCenterX  + 10, robotCenterY, 5, 5);
    }

    private void drawRect(Graphics g, int x, int y){
        g.drawRect(x-50,y-50, 100, 100);
    }
    
    private void drawTarget(Graphics2D g, int x, int y)
    {
        AffineTransform t = AffineTransform.getRotateInstance(0, 0, 0); 
        g.setTransform(t);
        g.setColor(Color.GREEN);
        fillOval(g, x, y, 5, 5);
        g.setColor(Color.BLACK);
        drawOval(g, x, y, 5, 5);
    }


}

package intersection_master.intersection;

import javax.swing.*;

public class Point extends JComponent {
    double x, y;

    public Point(double newX, double newY) {
        x = newX;
        y = newY;
    }

    @Override
    public String toString() {
        return "Point{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
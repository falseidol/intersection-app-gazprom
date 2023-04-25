package intersection_master.intersection;

import intersection_master.intersection.storage.PointDBStorage;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
import java.util.Scanner;

public class Main extends JComponent {
    private static final LinkedList<Point> lines = new LinkedList<>();
    private static final LinkedList<Point> intersected = new LinkedList<>();
    static Point p22 = new Point(0, 0);
    static Point p33 = new Point(0, 0);
    // Для работы с базой данных используем jdbc
    static final JdbcTemplate jdbcTemplate = new JdbcTemplate();
    static PointDBStorage pointStorage = new PointDBStorage(jdbcTemplate);

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        double leftLimit = 1;
        double rightLimit = 100;
        // считываем координаты для первого отрезка и для второго
        // остальные задаем случайным образом для удобства сделаем предел от 1 до 100
        double generatedLong = leftLimit + (Math.random() * (rightLimit - leftLimit));
        double generatedLong2 = leftLimit + (Math.random() * (rightLimit - leftLimit));
        double generatedLong3 = leftLimit + (Math.random() * (rightLimit - leftLimit));
        double generatedLong4 = leftLimit + (Math.random() * (rightLimit - leftLimit));
        Point p1 = new Point(scanner.nextDouble(), scanner.nextDouble());
        Point p2 = new Point(generatedLong, generatedLong2); // that point or
        Point p3 = new Point(scanner.nextDouble(), scanner.nextDouble()); // that
        Point p4 = new Point(generatedLong3, generatedLong4);
        // отрезки отображаются только если есть точка пересечения
        if (!checkIntersectionOfTwoLineSegments(p1, p2, p3, p4)) {
            System.out.println("Отрезки не пересекаются");
            System.exit(1);
        }
        // добавление конечных сгенерированных точек в базу данных
        pointStorage.addPoint(p1);
        pointStorage.addPoint(p4);
        // Отображение отрезков, метод будет работать, но некорректно т.к есть проблемы с этапом задачи, метод segmentDivider
        // В каждой точке пересечения один из отрезков должен быть разделен на два таким образом, чтобы образовался зазор в 2 единицы между его концами
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    // усыпляем поток, который отображает отрезки чтобы main успел заполнить lines и intersected
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                JFrame frame = new JFrame();
                JPanel panel = new JPanel(null) {
                    @Override
                    public void paint(Graphics g) {
                        super.paint(g);
                        g.setColor(Color.blue);
                        g.drawLine((int) lines.get(0).x, (int) lines.get(0).y, (int) lines.get(1).x, (int) lines.get(1).y);
                        g.setColor(Color.red);
                        g.drawLine((int) intersected.get(0).x, (int) intersected.get(0).y, (int) intersected.get(1).x, (int) intersected.get(1).y);
                    }
                };
                frame.add(panel);
                frame.setSize(400, 400);
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
    }

    /**
     * метод определения пересечения отрезков
     */
    private static boolean checkIntersectionOfTwoLineSegments(Point p1, Point p2,
                                                              Point p3, Point p4) {
        // Изначальная точка находится левее конечной относительно оси X, если ввод неверен, меняем местами
        if (p2.x < p1.x) {
            Point tmp = p1;
            p1 = p2;
            p2 = tmp;
        }
        if (p4.x < p3.x) {

            Point tmp = p3;
            p3 = p4;
            p4 = tmp;
        }
        // Если конец первого отрезка находится левее начала правого отрезка по X, то отрезки точно не имеют точки пересечения
        if (p2.x < p3.x) {
            return false;
        }
        // Частный случай вертикальные отрезки(Отрезок вертикальный, тогда и только тогда, когда абсциссы его обеих точек равны)
        // проверка вертикальности
        if ((p1.x - p2.x == 0) && (p3.x - p4.x == 0)) {
            // если они лежат на одном X
            if (p1.x == p3.x) {
                // Проверим пересекаться ли они, т.е. есть ли у них общий Y
                // для этого возьмём отрицание от случая, когда они НЕ пересекаются
                if (!((Math.max(p1.y, p2.y) < Math.min(p3.y, p4.y)) ||
                        (Math.min(p1.y, p2.y) > Math.max(p3.y, p4.y)))) {
                    System.out.println("1 случай");
                    System.out.println(p1.x + " Координата по X");
                    System.out.println(p1.y + " Координата по Y");
                    return true;
                }
            }
            return false;
        }
        // найдём коэффициенты уравнений, содержащих отрезки
        // f1(x) = A1*x + b1 = y
        // f2(x) = A2*x + b2 = y

        // если первый отрезок вертикальный
        if (p1.x - p2.x == 0) {
            // найдём Xa, Ya - точки пересечения двух прямых
            double Xa = p1.x;
            double A2 = (p3.y - p4.y) / (p3.x - p4.x);
            double b2 = p3.y - A2 * p3.x;
            double Ya = A2 * Xa + b2;
            if (p3.x <= Xa && p4.x >= Xa && Math.min(p1.y, p2.y) <= Ya &&
                    Math.max(p1.y, p2.y) >= Ya) {
                System.out.println("2 случай");
                System.out.println(Xa + " Координата по X");
                System.out.println(Ya + " Координата по Y");
                // проверяем чей угол наклона к оси больше
                checkAngleToOx(Xa, Ya, p1, p2, p3, p4);
                return true;
            }
            return false;
        }
        // если второй отрезок вертикальный
        if (p3.x - p4.x == 0) {
            // найдём Xa, Ya - точки пересечения двух прямых
            double Xa = p3.x;
            double A1 = (p1.y - p2.y) / (p1.x - p2.x);
            double b1 = p1.y - A1 * p1.x;
            double Ya = A1 * Xa + b1;
            if (p1.x <= Xa && p2.x >= Xa && Math.min(p3.y, p4.y) <= Ya &&
                    Math.max(p3.y, p4.y) >= Ya) {
                System.out.println("3 случай");
                System.out.println(Xa + " Координата по X");
                System.out.println(Ya + " Координата по Y");
                // проверяем чей угол наклона к оси больше
                checkAngleToOx(Xa, Ya, p1, p2, p3, p4);
                return true;
            }
            return false;
        }
        // оба отрезка невертикальные
        double A1 = (p1.y - p2.y) / (p1.x - p2.x);
        double A2 = (p3.y - p4.y) / (p3.x - p4.x);
        double b1 = p1.y - A1 * p1.x;
        double b2 = p3.y - A2 * p3.x;
        if (A1 == A2) {
            return false; // отрезки параллельны
        }
        // Xa - абсцисса точки пересечения двух прямых
        double Xa = (b2 - b1) / (A1 - A2);
        double Ya = A1 * Xa + b1;
        if ((Xa < Math.max(p1.x, p3.x)) || (Xa > Math.min(p2.x, p4.x))) {
            return false; // точка Xa находится вне пересечения проекций отрезков на ось X
        } else {
            System.out.println("4 случай");
            System.out.println(Xa + " Координата по X");
            System.out.println(Ya + " Координаты по Y");
            checkAngleToOx(Xa, Ya, p1, p2, p3, p4);
            return true;
        }
    }

    /**
     * Метод разделения отрезков, работает неверно
     * не получается найти координаты точки после того как мы отняли 2 единицы от длины отрезка
     */
    public static void segmentDivider(double XIntersection,
                                      double YIntersection,
                                      Point p1, Point p2) {
        // находим длину от начала отрезка до точки пересечения для разделения целого отрезка
        // по формуле d = √((x2-x1)²+(y2-y1)²), где d — рассчитываемый отрезок, x1,x2 — абсциссы начала и конца отрезка
        // y1,y2 — ординаты начала и конца отрезка.
        double lengthFirstPointToIntersection = Math.pow((XIntersection - p1.x) + (YIntersection - p1.y), 2);
        lengthFirstPointToIntersection = Math.sqrt(lengthFirstPointToIntersection);
        double lengthIntersectionToSecondPoint = Math.pow((p2.x - XIntersection) + (p2.y - YIntersection), 2);
        lengthIntersectionToSecondPoint = Math.sqrt(lengthIntersectionToSecondPoint);
        // угол наклона прямой вычисляется как arctg (y2-y1/x2-x1);
        double segmentAngleToOx = Math.atan((p2.y - p1.y) / (p2.x - p1.x));
        // если длина от начала до точки пересечения больше двух можем построить новый отрезок
        // 1 точка у нас есть, нужно получить вторую формулой ниже
        // x1 + (length - 2) * Math.cos(Math.toRadians(segmentAngleToOx))
        // y1 + (length -2) * Math.sin(Math.toRadians(angle)))
        // рассматриваем вариант когда левая длина больше 2
        double xCoordinate = (lengthFirstPointToIntersection - 2) * Math.cos(Math.toRadians(segmentAngleToOx));
        double yCoordinate = (lengthFirstPointToIntersection - 2) * Math.sin(Math.toRadians(segmentAngleToOx));
        if (lengthFirstPointToIntersection > 2) {
            p22.x = p1.x + xCoordinate;
            p22.y = p1.y + yCoordinate;
            intersected.add(p1);
            intersected.add(p22);
            // Добавление новой точки в базу данных
            pointStorage.addIntersectionPoints(p1, p22);
            // рассматриваем вариант когда правая длина больше 2
        } else if (lengthIntersectionToSecondPoint > 2) {
            p33.x = p2.x + xCoordinate;
            p33.y = p2.y + yCoordinate;
            intersected.add(p33);
            intersected.add(p2);
            // Добавление новой точки в базу данных
            pointStorage.addIntersectionPoints(p33, p2);
        } else {
            System.out.println("Длина отрезка меньше 2 единиц");
        }
    }

    /**
     * Метод проверки угла
     */
    public static void checkAngleToOx(double Xa, double Ya, Point p1, Point p2, Point p3, Point p4) {
        // выбираем какой из отрезков уменьшить на две единицы в зависимости от угла
        double segment1AngleToOx = Math.atan((p2.y - p1.y) / (p2.x - p1.x));
        double segment2AngleToOx = Math.atan((p4.y - p3.y) / (p4.x - p3.x));
        if (segment1AngleToOx > segment2AngleToOx) {
            lines.add(p3);
            lines.add(p4);
            segmentDivider(Xa, Ya, p1, p2);
            System.out.println("Разделяем 1 отрезок");
            // если угол равен выбираем любой
        } else if (segment1AngleToOx == segment2AngleToOx) {
            segmentDivider(Xa, Ya, p1, p2);
            lines.add(p3);
            lines.add(p4);
            System.out.println("Разделяем 1 отрезок");
        } else {
            segmentDivider(Xa, Ya, p3, p4);
            lines.add(p1);
            lines.add(p2);
            System.out.println("Разделяем 2 отрезок");
        }
    }
}
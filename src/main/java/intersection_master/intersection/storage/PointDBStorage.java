package intersection_master.intersection.storage;

import intersection_master.intersection.Point;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

public class PointDBStorage {
    private final JdbcTemplate jdbcTemplate;

    public PointDBStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    public Point addPoint(Point point) {
        final String sqlQuery = "INSERT INTO segments (x2, y2) " +
                "VALUES ( ?, ?)";
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery);
            stmt.setDouble(1, point.getX());
            stmt.setDouble(2, point.getY());
            return stmt;
        });
        return point;
    }

    public List<Point> addIntersectionPoints(Point p1, Point p2) {
        final String sqlQuery = "INSERT INTO intersection (x1, y1, x2, y2) " +
                "VALUES ( ?, ?, ?, ?)";
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery);
            stmt.setDouble(1, p1.getX());
            stmt.setDouble(2, p1.getY());
            stmt.setDouble(3, p2.getX());
            stmt.setDouble(4, p2.getY());
            return stmt;
        });
        List<Point> points = new ArrayList<>();
        points.add(p1);
        points.add(p2);
        return points;
    }
}
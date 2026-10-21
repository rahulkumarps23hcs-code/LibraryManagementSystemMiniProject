package com.Libraryapp.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.Libraryapp.model.Admin;
import com.Libraryapp.model.Member;
import com.Libraryapp.model.User;
import com.Libraryapp.util.DBConnection;

public class UserDAO {

    public int registerUser(User u) throws SQLException {
        String sql = "INSERT INTO user(name,email,mobile,address,password,role) VALUES(?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, u.getName());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getMobile());
            ps.setString(4, u.getAddress());
            ps.setString(5, u.getPassword());
            ps.setString(6, u.getRole());
            return ps.executeUpdate();
        }
    }

    public User login(String email, String password, String role) throws SQLException {
        String sql = "SELECT * FROM user WHERE email=? AND password=? AND role=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, password);
            ps.setString(3, role);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapUser(rs) : null;
            }
        }
    }

    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT 1 FROM user WHERE email=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public List<User> getAllMembers() throws SQLException {
        String sql = "SELECT * FROM user WHERE role='MEMBER'";
        List<User> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapUser(rs));
            }
        }
        return list;
    }

    public List<User> searchMembers(String keyword) throws SQLException {
        String sql = "SELECT * FROM user WHERE role='MEMBER' AND (name LIKE ? OR email LIKE ? OR mobile LIKE ?)";
        List<User> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String like = "%" + keyword + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapUser(rs));
                }
            }
        }
        return list;
    }

    private User mapUser(ResultSet rs) throws SQLException {
        int id = rs.getInt("user_id");
        String name = rs.getString("name");
        String email = rs.getString("email");
        String mobile = rs.getString("mobile");
        String address = rs.getString("address");
        String password = rs.getString("password");
        if ("ADMIN".equals(rs.getString("role"))) {
            return new Admin(id, name, email, mobile, address, password);
        }
        return new Member(id, name, email, mobile, address, password);
    }
}

package com.dao;

import com.pojo.Student;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface studentDao {
    List<Student> selectAllStudent();
}

package com.service;

import com.pojo.Student;

import java.io.FileNotFoundException;
import java.util.List;

public interface stuservice {
    List<Student> selectAllStudent() throws FileNotFoundException;
}

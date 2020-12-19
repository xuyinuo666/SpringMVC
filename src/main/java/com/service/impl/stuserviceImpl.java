package com.service.impl;

import com.dao.studentDao;
import com.pojo.Student;
import com.service.stuservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class stuserviceImpl implements stuservice {
    @Autowired
    @Qualifier("studentDao")
    private studentDao dao;
    @Override
    public List<Student> selectAllStudent() {
        List<Student> students = dao.selectAllStudent();
        for (Student student : students) {
            System.out.println(student);
        }
        return students;
    }
}

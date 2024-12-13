package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     * @param username
     * @return
     */
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);
    /*新增员工*/
    @Insert("insert into employee (name, username, password, phone, sex, id_number, create_time, update_time, create_user, update_user,status) " +
            "values" +
            "(#{name},#{username},#{password},#{phone},#{sex},#{idNumber},#{createTime},#{updateTime},#{createUser},#{updateUser},#{status})")
    void insert(Employee employee);


    /*分页查询，其实本质也就是查询，分页的操作在service层进行*/
    //这里需要进行动态查询，因此需要建立对应的xml文件来编写sql语言

    Page<Employee> pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

}

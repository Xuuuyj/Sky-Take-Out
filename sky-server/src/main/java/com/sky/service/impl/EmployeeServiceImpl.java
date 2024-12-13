package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();
        //传过来的明文密码需要进行加密

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // TODO 后期需要进行md5加密，然后再进行比对
        //加密处理
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    @Override
    public void save(EmployeeDTO employeeDTO){
        //这里有一个变化就是要把EmployeeDTO类转换成Employee类
        Employee employee = new Employee();
        //因为是要往Employee表里面插入数据
        //工具包里面有对象属性拷贝函数
        BeanUtils.copyProperties(employeeDTO, employee);

        //填补缺少的属性
        //设置账号状态，默认正常状态
        //这里因为很多实体都会涉及到状态，这里就在sky-common模块下com.sky.constants包下面已经定义了一个类
        employee.setStatus(StatusConstant.ENABLE);
        //设置密码，默认123456，记得加密
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));

        //记录当前的修改时间以及创建时间
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        //设置当前创始人id以及修改人的id，设置一个假数据即可，后面会进行修改
        //解决办法 ThreadLocal ,在拦截器里面讲empID存入，在这里面进行截取
        employee.setUpdateUser(BaseContext.getCurrentId());
        employee.setUpdateUser(BaseContext.getCurrentId());


        employeeMapper.insert(employee);
    }


    @Override
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        //使用分页查询工具
        PageHelper pageHelper = new PageHelper();
        pageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());
        //mapper层的返回结果
        Page<Employee> employees = employeeMapper.pageQuery(employeePageQueryDTO);

        long total = employees.getTotal();
        List<Employee> result = employees.getResult();

        return new PageResult(total, result);
    }

    @Override
    public void startOrStop(Integer status,Long id){
        //注意 很多时候逻辑要清晰，最终数据库里面的内容是需要修改的，你这样仅仅是反馈到前端修改了
        //builder是什么方法？？
        //@builder构建器注解！！！
        Employee employee = Employee.builder().status(status).id(id).build();

        //更新操作时间
        employee.setUpdateTime(LocalDateTime.now());
        //更新操作者
        employee.setUpdateUser(BaseContext.getCurrentId());

        //mapper层更新数据库里面的内容
        employeeMapper.update(employee);
    }
    public Employee getById(Long id){
        Employee employee = employeeMapper.getById(id);
        //设置密码不显示
        employee.setPassword("****");
        return employee;
    }

    public void update(EmployeeDTO employeeDTO){
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);

        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(BaseContext.getCurrentId());

        employeeMapper.update(employee);
    }

}

package ru.grniko.user.servlet;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import ru.grniko.user.dto.UserForm;
import ru.grniko.user.dto.UserForm.UserFormBuilder;
import ru.grniko.user.model.User;
import ru.grniko.user.repository.UsersRepository;
import ru.grniko.user.repository.UsersRepositoryImpl;
import ru.grniko.user.services.UserService;
import ru.grniko.user.services.UserServiceImpl;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;


@WebServlet("/")
public class CreateUserServlet extends HttpServlet {

    private HikariDataSource dataSource;
    private UserService userService;

    @Override
    public void init(ServletConfig config) {

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setUsername("postgres");
        hikariConfig.setPassword("112233");
        hikariConfig.setDriverClassName("org.postgresql.Driver");
        hikariConfig.setJdbcUrl("jdbc:postgresql://192.168.0.111:5432/andersendb");
        hikariConfig.setMaximumPoolSize(20);

        this.dataSource = new HikariDataSource(hikariConfig);

        UsersRepository usersRepository = new UsersRepositoryImpl(dataSource);
        this.userService = new UserServiceImpl(usersRepository);

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getServletPath();
        try {
            switch (action) {
                case "/create":
                    showNewForm(request, response);
                    break;
                case "/insert":
                    insert(request, response);
                    break;
                case "/delete":
                    deleteUser(request, response);
                    break;
                case "/update":
                    updateUser(request, response);
                    break;
                case "/edit":
                    showEditForm(request, response);
                    break;
                default:
                    listUser(request, response);
                    break;
            }
        } catch (SQLException ex) {
            throw new ServletException(ex);
        }
    }

    private void listUser(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        List<User> listUser = userService.getAll();
        request.setAttribute("listUser", listUser);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/user-list.jsp");
        dispatcher.forward(request, response);
    }

    private void showNewForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher("/jsp/user-form.jsp");
        dispatcher.forward(request, response);
    }

    private void insert(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String age = request.getParameter("age");

        UserFormBuilder builder = UserForm.builder();
        if (firstName != null) {
            builder.firstName(firstName);
        }
        if (lastName != null) {
            builder.lastName(lastName);
        }
        if (age != null) {
            int ageValue = Integer.parseInt(age);
            builder.age(ageValue);
        }

        UserForm userForm = builder.build();
        userService.createUser(userForm);
        response.sendRedirect("list");
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        Long id = Long.parseLong(request.getParameter("id"));
        User existingUser = userService.findById(id);
        RequestDispatcher dispatcher = request.getRequestDispatcher("jsp/user-form.jsp");
        request.setAttribute("user", existingUser);
        dispatcher.forward(request, response);

    }

    private void updateUser(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        Long userId = Long.parseLong(request.getParameter("id"));
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        Integer age = Integer.parseInt(request.getParameter("age"));

        User newUser = new User(userId, firstName, lastName, age, false);
        userService.updateUser(newUser);
        response.sendRedirect("list");
    }

    private void deleteUser(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        Long id = Long.parseLong(request.getParameter("id"));
        userService.deleteById(id);
        response.sendRedirect("list");
    }

    @Override
    public void destroy() {
        dataSource.close();
    }
}
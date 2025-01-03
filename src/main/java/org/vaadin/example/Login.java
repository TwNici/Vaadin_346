package org.vaadin.example;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.data.binder.Binder;
import org.vaadin.example.jwt.JwtUtil;

@Route("")
public class Login extends Div {

    private TextField firstName;
    private TextField lastName;
    private TextField username;
    private PasswordField password;
    private final Binder<User> binder;

    public Login() {
        this.binder = new Binder<>();
        init();
    }

    private void init() {
        initLayout();
        initBinder();
    }

    private void initBinder() {
        binder.forField(firstName).asRequired().bind(User::getFirstName, User::setFirstName);
        binder.forField(lastName).asRequired().bind(User::getLastName, User::setLastName);
        binder.forField(username).asRequired().bind(User::getUsername, User::setUsername);
        binder.forField(password).asRequired().bind(User::getPassword, User::setPassword);
    }

    private void initLayout() {
        firstName = new TextField("First name");
        lastName = new TextField("Last name");
        username = new TextField("Username");
        password = new PasswordField("Password");

        FormLayout formLayout = new FormLayout();
        formLayout.add(firstName, lastName, username, password);
        formLayout.setResponsiveSteps(
                new ResponsiveStep("0", 1),
                new ResponsiveStep("500px", 2));
        formLayout.setColspan(username, 2);

        Button login =  new Button(new Icon(VaadinIcon.ACCESSIBILITY), e -> login());

        add(formLayout, login);
    }

    public void login() {
        String token = JwtUtil.generateToken(binder.getBean().getUsername());
        UI.getCurrent().getPage().executeJs("localStorage.setItem('authToken', $0);", token);
        UI.getCurrent().navigate("login");
    }
}
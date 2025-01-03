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
import com.vaadin.flow.server.VaadinSession;
import org.vaadin.example.jwt.JwtUtil;

@Route("")
public class Login extends Div {

    private TextField firstName;
    private String name;

    public Login() {
        init();
    }

    private void init() {
        initLayout();
    }

    private void initLayout() {
        firstName = new TextField("First name");

        FormLayout formLayout = new FormLayout();
        formLayout.add(firstName);

        Button login =  new Button(new Icon(VaadinIcon.ACCESSIBILITY), e -> login());

        firstName.addValueChangeListener(e -> {
            name = e.getValue();
        });

        add(formLayout, login);
    }

    public void login() {
        if (name.equals("seeli")) {
            String token = JwtUtil.generateToken(name);
            VaadinSession.getCurrent().setAttribute("authToken", token);
        }
        UI.getCurrent().navigate("home");
    }
}
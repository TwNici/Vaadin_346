package org.vaadin.example;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("")
@PageTitle("Server Dashboard")
@CssImport("./themes/style.css")
public class MainView extends VerticalLayout {

    private Button Start;

    private Button Stop;

    private Button Restart;

    public MainView() {

        H1 title = new H1("Server Dashboard");
        title.addClassName("title");

        Button start =  new Button(new Icon(VaadinIcon.CARET_RIGHT), e -> serverStarter());
        title.addClassName("Start");

        Button stop = new Button(new Icon(VaadinIcon.STOP), e -> serverStopper());
        stop.addClassName("stop");

        Button restart = new Button(new Icon(VaadinIcon.REFRESH), e -> serverRestarter());
        restart.addClassName("restart");

        add(title, start, stop, restart);




    }

    public void serverStarter() {
        Notification.show("Server Startet");

    }

    public void serverStopper() {
        Notification.show("Server Stoppt");

    }

    public void serverRestarter() {
        Notification.show("Server Restartet");

    }
}

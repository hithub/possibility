package com.mjs_svc.possibility;

import com.mjs_svc.possibility.util.*;
import com.mjs_svc.possibility.views.*;
import com.mjs_svc.possibility.controllers.*;
import com.mjs_svc.possibility.models.*;

import java.awt.Color;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;

import java.beans.PropertyVetoException;
import org.hibernate.HibernateException;
import org.hibernate.Session;

/**
 * Soon to be the main app, but now just a tiny testbed
 *
 * @author Matthew Scott
 * @version $Id$
 */
public class App extends JFrame implements UserListener {
    // A user object to be used for keeping track of a logged-in user
    private User user;

    // GUI objects
    private JMenuBar menuBar;
    private JMenu fileMenu, editMenu, viewMenu, toolsMenu, helpMenu,
            viewPeople, vpEmployees, vpCustomers, vpCreators,
            viewProducts, viewOrders, viewTransactions, viewTroubleTickets;
    private JMenuItem
            f_settings, f_login, f_logout, f_clockin, f_clockout, f_exit, //file
            vpe_all, vpe_filter, vpe_new,           //employees
            vpcus_all, vpcus_filter,                //customers
            vpcr_all, vpcr_filter, vpcr_new,        //creators
            vpr_all, vpr_filter, vpr_new,           //products
            vo_all, vo_filter, vo_new,              //orders
            vtr_all, vtr_filter, vtr_new,           //transactions
            vtt_all, vtt_mine, vtt_unassigned,
            vtt_filter, vtt_new,                    //troubletickets
            t_changePass;                           //tools
    private JDesktopPane desktop;

    // Some panel stuff for User and Status listeners/containers
    private Login loginPanel = new Login(); // needed for setting user listener
    private TimeClockController timeClock = new TimeClockController();
    private StatusBar statusBar = new StatusBar();

    // Resource bundles for i18n
    private ResourceBundle menuRB = ResourceBundle.getBundle(
            "MenuTexts",
            Locale.getDefault()),
            statusRB = ResourceBundle.getBundle(
            "StatusTexts",
            Locale.getDefault()),
            panelRB = ResourceBundle.getBundle(
            "PanelTexts",
            Locale.getDefault());

    /**
     * Set up the app with the system L&F
     * @param args Command line args (none defined)
     */
    public static void main( String[] args ) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Throwable e) {
            // Ignore the exception and live with Metal
        }
        new App().setVisible(true);
    }

    /**
     * Construct a new App with a splash screen, all done in methods
     */
    public App() {
        SplashScreen splash = new SplashScreen();

        // Call getCurrentSession() to start a session
        splash.updateProgress(1, statusRB.getString("splash.hibernate"));
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();

        // Set up containers
        splash.updateProgress(2, statusRB.getString("splash.containers"));
        StatusContainer.setStatusBar(statusBar);
        UserContainer.setUser(new User());
        loginPanel.setUserListener(this);
        timeClock.setUserListener(this);

        // Set up components
        splash.updateProgress(3, statusRB.getString("splash.components"));
        initComponents();

        // This shouldn't really ever be seen :3
        splash.updateProgress(4, statusRB.getString("splash.finished"));
        splash.setVisible(false);
        splash.dispose();
    }

    /**
     * Build all components and add to the frame
     */
    private void initComponents() {
        desktop = new JDesktopPane();
        desktop.setBackground(Color.WHITE);

        menuBar = new JMenuBar();

        // Set up file menus
        fileMenu = new JMenu(menuRB.getString("file"));
        f_login = new JMenuItem(menuRB.getString("file.login"));
        f_login.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                @SuppressWarnings("static-access")
                JInternalFrame login = new JInternalFrame(
                        loginPanel.title,
                        loginPanel.resizable,
                        loginPanel.closable,
                        loginPanel.maximizable,
                        loginPanel.iconifiable);
                login.setContentPane(loginPanel);
                login.pack();
                login.setVisible(true);
                login.setSize(loginPanel.getSize());
                desktop.add(login);
                try {
                    login.setSelected(true);
                } catch (PropertyVetoException exc) {
                    //
                }
            }
        });
        fileMenu.add(f_login);
        f_logout = new JMenuItem(menuRB.getString("file.logout"));
        f_logout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (user.getIsAuthenticated()) {
                    user.deauthenticate();
                    setUser(user);
                    setTitle("POSsibility");
                    statusBar.setStatus(statusRB.getString("onlogout"));
                    f_logout.setEnabled(false);
                    f_login.setEnabled(true);
                    f_clockin.setEnabled(false);
                    f_clockout.setEnabled(false);
                }
            }
        });
        f_logout.setEnabled(false);
        fileMenu.add(f_logout);
        f_clockin = new JMenuItem(menuRB.getString("file.clockin"));
        f_clockin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (timeClock.doClockIn(user)) {
                    statusBar.setStatus(statusRB.getString("onclockout.success"));
                } else {
                    statusBar.setStatus(statusRB.getString("onclockin.fail"));
                }
            }
        });
        f_clockin.setEnabled(false);
        fileMenu.add(f_clockin);
        f_clockout = new JMenuItem(menuRB.getString("file.clockout"));
        f_clockout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (timeClock.doClockOut(user)) {
                    statusBar.setStatus(statusRB.getString("onclockin.success"));
                } else {
                    statusBar.setStatus(statusRB.getString("onclockin.fail"));
                }
            }
        });
        f_clockout.setEnabled(false);
        fileMenu.add(f_clockout);
        f_settings = new JMenuItem(menuRB.getString("file.settings"));
        f_settings.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JInternalFrame settings = new JInternalFrame();
                settings.setContentPane(new Settings());
                settings.pack();
                settings.setVisible(true);
                desktop.add(settings);
            }
        });
        fileMenu.add(f_settings);
        fileMenu.addSeparator();
        f_exit = new JMenuItem(menuRB.getString("file.exit"));
        f_exit.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }

        });
        fileMenu.add(f_exit);
        menuBar.add(fileMenu);

        // Set up edit menus
        editMenu = new JMenu(menuRB.getString("edit"));
        menuBar.add(editMenu);

        // Set up view menus
        viewMenu = new JMenu(menuRB.getString("view"));
        viewPeople = new JMenu(menuRB.getString("view.people"));

        // Set up view.people.employee menus
        vpEmployees = new JMenu(menuRB.getString("view.people.employees"));
        vpe_all = new JMenuItem(menuRB.getString("view.people.employees.all"));
        vpe_all.addActionListener(new ActionListener () {
            @Override
            public void actionPerformed(ActionEvent e) {
                EmployeeList employees = new EmployeeList(panelRB.getString("all"), "");
                @SuppressWarnings("static-access")
                JInternalFrame allEmployees = new JInternalFrame(
                        employees.title,
                        employees.resizable,
                        employees.maximizable,
                        employees.iconifiable);
                allEmployees.setContentPane(employees);
                allEmployees.pack();
                allEmployees.setVisible(true);
                desktop.add(allEmployees);
            }
        });
        vpEmployees.add(vpe_all);
        vpe_filter = new JMenuItem(menuRB.getString("view.people.employees.filter"));
        vpEmployees.add(vpe_filter);
        vpEmployees.addSeparator();
        vpe_new = new JMenuItem(menuRB.getString("view.people.employees.new"));
        vpEmployees.add(vpe_new);
        viewPeople.add(vpEmployees);

        // Set up view.people.creator menus
        vpCreators = new JMenu(menuRB.getString("view.people.creators"));
        vpcr_all = new JMenuItem(menuRB.getString("view.people.creators.all"));
        vpCreators.add(vpcr_all);
        vpcr_filter = new JMenuItem(menuRB.getString("view.people.creators.filter"));
        vpCreators.add(vpcr_filter);
        vpCreators.addSeparator();
        vpcr_new = new JMenuItem(menuRB.getString("view.people.creators.new"));
        vpCreators.add(vpcr_new);
        viewPeople.add(vpCreators);

        // Set up view.people.customer menus
        vpCustomers = new JMenu(menuRB.getString("view.people.customers"));
        vpcus_all = new JMenuItem(menuRB.getString("view.people.customers.all"));
        vpcus_all.addActionListener(new ActionListener () {
            @Override
            public void actionPerformed(ActionEvent e) {
                CustomerList customers = new CustomerList(panelRB.getString("all"), "");
                @SuppressWarnings("static-access")
                JInternalFrame allCustomers = new JInternalFrame(
                        customers.title,
                        customers.resizable,
                        customers.maximizable,
                        customers.iconifiable);
                allCustomers.setContentPane(customers);
                allCustomers.pack();
                allCustomers.setVisible(true);
                desktop.add(allCustomers);
            }
        });
        vpCustomers.add(vpcus_all);
        vpcus_filter = new JMenuItem(menuRB.getString("view.people.customers.filter"));
        vpCustomers.add(vpcus_filter);
        viewPeople.add(vpCustomers);
        viewMenu.add(viewPeople);

        // Set up view.products menus
        viewProducts = new JMenu(menuRB.getString("view.products"));
        vpr_all = new JMenuItem(menuRB.getString("view.products.all"));
        viewProducts.add(vpr_all);
        vpr_filter = new JMenuItem(menuRB.getString("view.products.filter"));
        viewProducts.add(vpr_filter);
        viewProducts.addSeparator();
        vpr_new = new JMenuItem(menuRB.getString("view.products.new"));
        viewProducts.add(vpr_new);
        viewMenu.add(viewProducts);

        // Set up view.orders menus
        viewOrders = new JMenu(menuRB.getString("view.orders"));
        vo_all = new JMenuItem(menuRB.getString("view.orders.all"));
        viewOrders.add(vo_all);
        vo_filter = new JMenuItem(menuRB.getString("view.orders.filter"));
        viewOrders.add(vo_filter);
        viewOrders.addSeparator();
        vo_new = new JMenuItem(menuRB.getString("view.orders.new"));
        viewOrders.add(vo_new);
        viewMenu.add(viewOrders);

        // Set up view.transactions menus
        viewTransactions = new JMenu(menuRB.getString("view.transactions"));
        vtr_all = new JMenuItem(menuRB.getString("view.transactions.all"));
        viewTransactions.add(vtr_all);
        vtr_filter = new JMenuItem(menuRB.getString("view.transactions.filter"));
        viewTransactions.add(vtr_filter);
        viewTransactions.addSeparator();
        vtr_new = new JMenuItem(menuRB.getString("view.transactions.new"));
        viewMenu.add(viewTransactions);

        // Set up view.troubletickets menus
        viewTroubleTickets = new JMenu(menuRB.getString("view.troubletickets"));
        vtt_all = new JMenuItem(menuRB.getString("view.troubletickets.all"));
        viewTroubleTickets.add(vtt_all);
        vtt_mine = new JMenuItem(menuRB.getString("view.troubletickets.mine"));
        viewTroubleTickets.add(vtt_mine);
        vtt_unassigned = new JMenuItem(menuRB.getString("view.troubletickets.unassigned"));
        viewTroubleTickets.add(vtt_unassigned);
        vtt_filter = new JMenuItem(menuRB.getString("view.troubletickets.filter"));
        viewTroubleTickets.add(vtt_filter);
        viewTroubleTickets.addSeparator();
        vtt_new = new JMenuItem(menuRB.getString("view.troubletickets.new"));
        viewTroubleTickets.add(vtt_new);
        viewMenu.add(viewTroubleTickets);

        // Add view menu
        menuBar.add(viewMenu);

        // Set up tools menu
        toolsMenu = new JMenu(menuRB.getString("tools"));
        t_changePass = new JMenuItem(menuRB.getString("tools.changepassword"));
        toolsMenu.add(t_changePass);
        menuBar.add(toolsMenu);

        // Set up help menu
        helpMenu = new JMenu(menuRB.getString("help"));
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);

        // Set up default close operation to make sure hibernate session is
        // closed
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                try {
                    Session session = HibernateUtil.getSessionFactory().getCurrentSession();
                    session.close();
                } catch (HibernateException exc) {
                    //
                }
                System.exit(0);
            }
        });

        // Set the welcome message on the status bar
        statusBar.setStatus(statusRB.getString("welcome"));

        // Lay out the components
        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(desktop, GroupLayout.DEFAULT_SIZE, 800, Short.MAX_VALUE)
            .addComponent(statusBar, GroupLayout.DEFAULT_SIZE, 800, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createSequentialGroup()
            .addComponent(desktop, GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
            .addComponent(statusBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        );

        // Set the title
        setTitle("POSsibility");

        // Pack!
        pack();
        setLocationRelativeTo(null);
    }

    /**
     * Implement setUser - allows us to change the title and status bars
     * @param _user
     */
    @Override
    public void setUser(User _user) {
        user = _user;
        UserContainer.setUser(user);
        if (user.getIsAuthenticated()) {
            f_login.setEnabled(false);
            f_logout.setEnabled(true);
            setTitle("POSsibility (" + user.getUsername() + ")");
            if (user.getEmployee() instanceof Employee) {
                if (user.getEmployee().getIsClockedIn()) {
                    setTitle(getTitle() + " (clocked in)");
                    f_clockin.setEnabled(false);
                    f_clockout.setEnabled(true);
                } else {
                    f_clockin.setEnabled(true);
                    f_clockout.setEnabled(false);
                }
            }
        } else {
            loginPanel = new Login();
            loginPanel.setUserListener(this);
        }
    }
}

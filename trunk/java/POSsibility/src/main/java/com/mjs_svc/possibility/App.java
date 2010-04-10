package com.mjs_svc.possibility;

import com.mjs_svc.possibility.util.*;
import com.mjs_svc.possibility.views.*;
import com.mjs_svc.possibility.controllers.*;
import com.mjs_svc.possibility.models.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;
import org.hibernate.HibernateException;
import org.hibernate.Session;

/**
 * Soon to be the main app, but now just a tiny testbed
 *
 * @author Matthew Scott
 * @version $Id$
 */
public class App extends JFrame implements UserListener {
    private User user;
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
    private Login loginPanel = new Login(); // needed for setting user listener
    private TimeClockController timeClock = new TimeClockController();
    private StatusBar statusBar = new StatusBar();
    private ResourceBundle menuRB = ResourceBundle.getBundle(
            "MenuTexts",
            Locale.getDefault()),
            statusRB = ResourceBundle.getBundle(
            "StatusTexts",
            Locale.getDefault()),
            panelRB = ResourceBundle.getBundle(
            "PanelTexts",
            Locale.getDefault());

    public static void main( String[] args ) {
        new App().setVisible(true);
    }

    public App() {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        StatusContainer.setStatusBar(statusBar);
        loginPanel.setUserListener(this);
        timeClock.setUserListener(this);
        initComponents();
    }

    private void exitMenuItemActionPerformed(ActionEvent evt) {
        System.exit(0);
    }

    private void initComponents() {
        desktop = new JDesktopPane();
        menuBar = new JMenuBar();

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
                desktop.setSelectedFrame(login);
            }
        });
        fileMenu.add(f_login);
        f_logout = new JMenuItem(menuRB.getString("file.logout"));
        f_logout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (user.getIsAuthenticated()) {
                    user.deauthenticate();
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

        editMenu = new JMenu(menuRB.getString("edit"));
        menuBar.add(editMenu);

        viewMenu = new JMenu(menuRB.getString("view"));
        viewPeople = new JMenu(menuRB.getString("view.people"));
        vpEmployees = new JMenu(menuRB.getString("view.people.employees"));
        vpe_all = new JMenuItem(menuRB.getString("view.people.employees.all"));
        vpe_all.addActionListener(new ActionListener () {
            @Override
            public void actionPerformed(ActionEvent e) {
                EmployeesList employees = new EmployeesList(panelRB.getString("all"), "");
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

        vpCreators = new JMenu(menuRB.getString("view.people.creators"));
        vpcr_all = new JMenuItem(menuRB.getString("view.people.creators.all"));
        vpCreators.add(vpcr_all);
        vpcr_filter = new JMenuItem(menuRB.getString("view.people.creators.filter"));
        vpCreators.add(vpcr_filter);
        vpCreators.addSeparator();
        vpcr_new = new JMenuItem(menuRB.getString("view.people.creators.new"));
        vpCreators.add(vpcr_new);
        viewPeople.add(vpCreators);
        
        vpCustomers = new JMenu(menuRB.getString("view.people.creators"));
        vpcus_all = new JMenuItem(menuRB.getString("view.people.creators.all"));
        vpCustomers.add(vpcus_all);
        vpcus_filter = new JMenuItem(menuRB.getString("view.people.creators.filter"));
        vpCustomers.add(vpcus_filter);
        viewPeople.add(vpCustomers);
        viewMenu.add(viewPeople);

        viewProducts = new JMenu(menuRB.getString("view.products"));
        vpr_all = new JMenuItem(menuRB.getString("view.products.all"));
        viewProducts.add(vpr_all);
        vpr_filter = new JMenuItem(menuRB.getString("view.products.filter"));
        viewProducts.add(vpr_filter);
        viewProducts.addSeparator();
        vpr_new = new JMenuItem(menuRB.getString("view.products.new"));
        viewProducts.add(vpr_new);
        viewMenu.add(viewProducts);

        viewOrders = new JMenu(menuRB.getString("view.orders"));
        vo_all = new JMenuItem(menuRB.getString("view.orders.all"));
        viewOrders.add(vo_all);
        vo_filter = new JMenuItem(menuRB.getString("view.orders.filter"));
        viewOrders.add(vo_filter);
        viewOrders.addSeparator();
        vo_new = new JMenuItem(menuRB.getString("view.orders.new"));
        viewOrders.add(vo_new);
        viewMenu.add(viewOrders);

        viewTransactions = new JMenu(menuRB.getString("view.transactions"));
        vtr_all = new JMenuItem(menuRB.getString("view.transactions.all"));
        viewTransactions.add(vtr_all);
        vtr_filter = new JMenuItem(menuRB.getString("view.transactions.filter"));
        viewTransactions.add(vtr_filter);
        viewTransactions.addSeparator();
        vtr_new = new JMenuItem(menuRB.getString("view.transactions.new"));
        viewMenu.add(viewTransactions);

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

        menuBar.add(viewMenu);

        toolsMenu = new JMenu(menuRB.getString("tools"));
        t_changePass = new JMenuItem(menuRB.getString("tools.changepassword"));
        toolsMenu.add(t_changePass);
        menuBar.add(toolsMenu);

        helpMenu = new JMenu(menuRB.getString("help"));
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);

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

        statusBar.setStatus(statusRB.getString("welcome"));

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

        setTitle("POSsibility");

        pack();
    }

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
        }
    }
}
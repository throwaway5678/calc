package com.awesomecalc.awesomecalculator;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import java.util.Stack;



public class MainActivity extends Activity {

    //backstack of the previous menu-layout-ids of the drawer
    //a layout-id is popped from the stack and inflated in the drawer when user presses the backbutton
    private Stack<Integer> mDrawerMenuBackstack;
    private Stack<String> mFragmentTitleBackstack;
    private Stack<String> mMenuTitleBackstack;

    //view variables
    private DrawerContainerLayout mNavigationDrawerContainer;
    private DrawerLayout mDrawerLayoutManager;

    //fragments
    private CalculatorFragment mCalcFragment;
    private BinomialFragment mBinomiFragment;

    private void changeTitleBacktoPreviousTitle() {
        if(mDrawerLayoutManager.isDrawerOpen(Gravity.LEFT)) {
            String current = mMenuTitleBackstack.pop();
            String previous = mMenuTitleBackstack.lastElement();
            setTitle(previous);
        } else {
            String current = mFragmentTitleBackstack.pop();
            String previous = mFragmentTitleBackstack.lastElement();
            setTitle(previous);
        }
    }

    private void changeMainFragmentTo(Fragment newFrag)
    {
        FragmentManager fragManager = getFragmentManager();
        FragmentTransaction fragTransaction = fragManager.beginTransaction();

        //set animations before replacing!
        fragTransaction.setCustomAnimations(R.animator.main_fragment_slide_in_top, R.animator.main_fragment_slide_out_bottom, R.animator.main_fragment_slide_in_bottom , R.animator.main_fragment_slide_out_top);

        fragTransaction.replace(R.id.main_fragment_container, newFrag);


        if(fragManager.getBackStackEntryCount() == 0)
        {
            //add to backstack so the user can navigate back
            //set the backstackEntry name to our class name, so we can identify which fragment it is later
            //this way we can ensure, that the same fragment is not added to the back stack multiple times
            fragTransaction.addToBackStack(newFrag.getClass().getName());
        }
            //test if the name that of the backstackEntry on top of the stack, which we set in the addToBackstack() method, is the same of our class
            //if it is the same, the user tries to push the same fragment on the stack times in a row
        else if( !fragManager.getBackStackEntryAt(fragManager.getBackStackEntryCount()-1).getName().equals(newFrag.getClass().getName()) )
        {
            //add to backstack so the user can navigate back
            //set the backstackEntry name to our class name, so we can identify which fragment it is later
            //this way we can ensure, that the same fragment is not added to the back stack multiple times
            fragTransaction.addToBackStack(newFrag.getClass().getName());
        }

        fragTransaction.commit();
    }

    private void changeDrawerMenuLayoutTo(int newMenuLayoutId)
    {

        View currentMenu = mNavigationDrawerContainer.getChildAt(0); //get currently displayed menu
        View newMenu = getLayoutInflater().inflate(newMenuLayoutId, null); //make new menu that is to be displayed

        //place new menu "outside" of drawer
        newMenu.setY(0);
        newMenu.setX(mNavigationDrawerContainer.getWidth());

        mNavigationDrawerContainer.addView(newMenu, mNavigationDrawerContainer.getWidth(), mNavigationDrawerContainer.getHeight());

        //new menu flies in from right
        //old menu flies out to the left

        currentMenu.animate()
                .x(-mNavigationDrawerContainer.getWidth())
                .setDuration(250);

        newMenu.animate()
                .x(0)
                .setDuration(250)
                .withEndAction(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        //remove old menu after transition animation
                        mNavigationDrawerContainer.removeViewAt(0);
                    }
                });
    }

    private void changeDrawerBackToPreviousMenu()
    {
        //TODO: kinda a duplicate of changeDrawerMenuLayoutTo() with reversed animation

        if(!mDrawerMenuBackstack.empty())
        {
            View currentMenu = mNavigationDrawerContainer.getChildAt(0); //get currently displayed menu
            View previousMenu = getLayoutInflater().inflate(mDrawerMenuBackstack.pop(), null); //make new menu that is to be displayed (pop layout from the previous menu off the backs tack)

            //place new menu "outside" of drawer
            previousMenu.setY(0);
            previousMenu.setX(-mNavigationDrawerContainer.getWidth());

            mNavigationDrawerContainer.addView(previousMenu, mNavigationDrawerContainer.getWidth(), mNavigationDrawerContainer.getHeight());

            //new menu flies in from right
            //old menu flies out to the left

            currentMenu.animate()
                    .x(mNavigationDrawerContainer.getWidth())
                    .setDuration(300);

            previousMenu.animate()
                    .x(0)
                    .setDuration(300)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run()
                        {
                            //remove old menu after transition animation
                            mNavigationDrawerContainer.removeViewAt(0);
                        }
                    });
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_main);


        //init - members
        mDrawerMenuBackstack = new Stack<Integer>();
        mMenuTitleBackstack = new Stack<String>();
        mFragmentTitleBackstack = new Stack<String>();
        mNavigationDrawerContainer = (DrawerContainerLayout)findViewById(R.id.navigation_drawer_container);
        mDrawerLayoutManager = (DrawerLayout)findViewById(R.id.drawer_layout_manager);
        mCalcFragment = new CalculatorFragment();
        mBinomiFragment = new BinomialFragment();

        //init - inflate main menu in the drawer
        LayoutInflater inflater = getLayoutInflater();
        mNavigationDrawerContainer.addView(inflater.inflate(R.layout.drawer_menu_main, null));

        //init - display home fragment
        FragmentTransaction fragTransaction = getFragmentManager().beginTransaction();
        fragTransaction.replace(R.id.main_fragment_container, mCalcFragment);
        getFragmentManager().getBackStackEntryCount();
        fragTransaction.commit();

        mNavigationDrawerContainer.setSwipeRightListener(new Runnable() {
            @Override
            public void run()
            {
                //only use the swipe-to-go-back gesture when drawer displays a submenu
                if(mMenuTitleBackstack.size() > 1)
                    onBackPressed();
            }
        });

        mDrawerLayoutManager.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                setTitle(mMenuTitleBackstack.lastElement());
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                setTitle(mFragmentTitleBackstack.lastElement());
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        // init - titles
        mFragmentTitleBackstack.push("Calculator");
        mMenuTitleBackstack.push("Main Menu");
        setTitle("Calculator");
    }

    @Override
    public void onBackPressed()
    {
        //close drawer if back-button is pressed and drawer is open
        if(mDrawerLayoutManager.isDrawerOpen(Gravity.LEFT))
        {
            //if backstack is empty, current menu is the topmost menu
            if(mDrawerMenuBackstack.empty())
            {
                mDrawerLayoutManager.closeDrawer(Gravity.LEFT);
                setTitle(mFragmentTitleBackstack.lastElement());
            }
            else //current menu is a submenu so we must navigate to the previous topmenu
            {
                changeDrawerBackToPreviousMenu();
                changeTitleBacktoPreviousTitle();
            }
        }
        else
        {
            //if fragment back stack is not empty -> open previous fragment
            if(getFragmentManager().getBackStackEntryCount() != 0)
            {
                //open previous fragment on the back stack
                getFragmentManager().popBackStack();
                changeTitleBacktoPreviousTitle();
            }
            else //fragment back stack is empty
            {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == R.id.action_settings)
        {
            // TODO: Open Settings activity here
            return true;
        }

        else if(id == R.id.action_help) {
            // TODO: Open Help activity here
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onDrawerMenuCalculatorClick(View sender)
    {
        //display calculator fragment
        changeMainFragmentTo(mCalcFragment);

        mDrawerLayoutManager.closeDrawer(Gravity.LEFT);
        mFragmentTitleBackstack.push("Calculator");
        setTitle("Calculator");
    }

    public void onDrawerMenuAlgebraClick(View sender)
    {
        //push old topmenu to the backstack before inflating new submenu, so we can later navigate back via backbutton
        mDrawerMenuBackstack.push(R.layout.drawer_menu_main);

        changeDrawerMenuLayoutTo(R.layout.drawer_submenu_algebra);
        mMenuTitleBackstack.push("Algebra");
        setTitle("Algebra");
    }

    public void onDrawerMenuAnalysisClick(View sender)
    {
        //push old topmenu to the backstack before inflating new submenu, so we can later navigate back via backbutton
        mDrawerMenuBackstack.push(R.layout.drawer_menu_main);

        changeDrawerMenuLayoutTo(R.layout.drawer_submenu_analysis);
        mMenuTitleBackstack.push("Analysis");
        setTitle("Analysis");
    }

    public void onDrawerMenuGeometryClick(View sender)
    {
        //push old topmenu to the backstack before inflating new submenu, so we can later navigate back via backbutton
        mDrawerMenuBackstack.push(R.layout.drawer_menu_main);

        changeDrawerMenuLayoutTo(R.layout.drawer_submenu_geometry);
        mMenuTitleBackstack.push("Analytic Geometry");
        setTitle("Analytic Geometry");
    }

    public void onDrawerMenuStochasticClick(View sender)
    {
        //push old topmenu to the backstack before inflating new submenu, so we can later navigate back via backbutton
        mDrawerMenuBackstack.push(R.layout.drawer_menu_main);

        changeDrawerMenuLayoutTo(R.layout.drawer_submenu_stochastic);
        mMenuTitleBackstack.push("Stochastic");
        setTitle("Stochastic");
    }

    public void onDrawerMenuUnitConversionClick(View sender)
    {
        //push old topmenu to the backstack before inflating new submenu, so we can later navigate back via backbutton
        mDrawerMenuBackstack.push(R.layout.drawer_menu_main);

        changeDrawerMenuLayoutTo(R.layout.drawer_submenu_unit_conversion);
        mMenuTitleBackstack.push("Unit Conversion");
        setTitle("Unit Conversion");
    }

    public void onDrawerMenuSettingsClick(View sender)
    {
        //start settings activity

        mDrawerLayoutManager.closeDrawer(Gravity.LEFT);
        mFragmentTitleBackstack.push("Settings");
        setTitle("Settings");
    }

    public void onDrawerSubMenuBinomialClick(View sender)
    {
        //display binomioal coefficient fragment
        changeMainFragmentTo(mBinomiFragment);

        mDrawerLayoutManager.closeDrawer(Gravity.LEFT);
        mFragmentTitleBackstack.push("Binomial Coefficient");
        setTitle("Binomial Coefficient");
    }

    public void onDrawerSubMenuSolveClick(View sender)
    {
        //TODO: method stub for drawer menu handler method
        Toast.makeText(this, "not implemented", Toast.LENGTH_SHORT);
    }
}

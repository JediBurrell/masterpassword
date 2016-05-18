package de.devland.masterpassword.ui.drawer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.base.Strings;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.devland.esperandro.Esperandro;
import de.devland.masterpassword.App;
import de.devland.masterpassword.R;
import de.devland.masterpassword.model.Category;
import de.devland.masterpassword.prefs.DefaultPrefs;
import de.devland.masterpassword.util.event.CategoryChangeEvent;
import de.devland.masterpassword.util.event.ReloadDrawerEvent;

/**
 * Created by deekay on 02/11/14.
 */
public class CategoryDrawerItem extends DrawerItem {

    protected final Category category;
    protected String firstLetter;

    protected Bus bus;
    protected DefaultPrefs defaultPrefs;

    protected View rootView;
    @BindView(R.id.textView_categoryLetter)
    protected TextView categoryLetter;
    @BindView(R.id.textView_settingsItem)
    protected TextView headerText;
    @BindView(R.id.imageView_deleteCategory)
    protected ImageView deleteCategory;

    protected boolean active = false;

    public CategoryDrawerItem(Category category) {
        super(DrawerItemType.CATEGORY);
        this.category = category;
        this.firstLetter = Strings.isNullOrEmpty(category.getName()) ? "" : category.getName().substring(0, 1);
        bus = App.get().getBus();
        bus.register(this);
        defaultPrefs = Esperandro.getPreferences(DefaultPrefs.class, App.get());
    }

    @Override
    protected void finalize() throws Throwable {
        bus.unregister(this);
        super.finalize();
    }

    @Override
    public View getView(Context context, ViewGroup root) {
        LayoutInflater inflater = LayoutInflater.from(context);
        rootView = inflater.inflate(R.layout.drawer_item_category, root, false);
        updateActiveState();
        ButterKnife.bind(this, rootView);
        headerText.setText(category.getName());
        categoryLetter.setText(firstLetter);
        return rootView;
    }

    private void updateActiveState() {
        if (rootView != null) {
            if (active) {
                rootView.setBackgroundResource(R.color.drawer_item_active);
            } else {
                rootView.setBackgroundResource(R.color.drawer_background);
            }
        }
    }

    @Override
    public void onClick(Context context) {
        bus.post(new CategoryChangeEvent(category));
    }

    @Subscribe
    public void activeCategoryChanged(CategoryChangeEvent event) {
        active = category.equals(event.getCategory());
        updateActiveState();
    }

    @OnClick(R.id.imageView_deleteCategory)
    public void onDeleteCategory() {
        List<Category> categories = defaultPrefs.categories();
        categories.remove(category);
        defaultPrefs.categories(categories);
        if (active) {
            bus.post(new CategoryChangeEvent(Category.all(App.get())));
        }
        bus.post(new ReloadDrawerEvent());

    }
}

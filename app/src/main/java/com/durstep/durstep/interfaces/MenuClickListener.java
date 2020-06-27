package com.durstep.durstep.interfaces;

import android.view.MenuItem;
import android.widget.PopupMenu;

import com.durstep.durstep.model.Subscription;

public interface MenuClickListener<T> {
    void onMenuItemClick(int id, T object);
}

package com.durstep.durstep.interfaces;

import android.view.MenuItem;
import android.widget.PopupMenu;

import com.durstep.durstep.model.Subscription;

public interface SubsMenuClickListener {
    void onMenuItemClick(int id, Subscription subscription);
}

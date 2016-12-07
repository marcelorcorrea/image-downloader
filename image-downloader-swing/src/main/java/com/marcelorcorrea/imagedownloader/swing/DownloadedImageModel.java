package com.marcelorcorrea.imagedownloader.swing;


import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;

import com.marcelorcorrea.imagedownloader.core.model.DownloadedImage;


/**
 * 
 * @author Marcelo Correa
 */
@SuppressWarnings({ "serial" })
class DownloadedImageModel extends AbstractListModel<DownloadedImage> {

	private final List<DownloadedImage> items = new ArrayList<>();

	public void addElement(DownloadedImage di) {
		items.add(di);
	}

	public void clear() {
		items.clear();
	}

	public DownloadedImage getElementAt(int index) {
		return items.get(index);
	}

	public int getSize() {
		return items.size();
	}

	public void update() {
		this.fireContentsChanged(this, 0, items.size() - 1);
	}
	
	public List<DownloadedImage> getElements(){
		return items;
	}
}

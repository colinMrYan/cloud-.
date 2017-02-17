package com.inspur.emmcloud.bean;

import org.json.JSONObject;

/**
 * classes : com.inspur.emmcloud.bean.AppTabBean Create at 2016年12月13日 下午2:53:44
 */
public class AppTabBean {

	private String title = "";
	private String image = "";
	private String selectedImage = "";
	private boolean selected = false;

	public AppTabBean() {
	}
	public AppTabBean(String response) {
		try {
			JSONObject jsonObject = new JSONObject(response);
			if (jsonObject.has("title")) {
				this.title = jsonObject.getString("title");
			}

			if (jsonObject.has("image")) {
				this.image = jsonObject.getString("image");
			}

			if (jsonObject.has("selectedImage")) {
				this.selectedImage = jsonObject.getString("selectedImage");
			}

			if (jsonObject.has("selected")) {
				this.selected = jsonObject.getBoolean("selected");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getSelectedImage() {
		return selectedImage;
	}

	public void setSelectedImage(String selectedImage) {
		this.selectedImage = selectedImage;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

}

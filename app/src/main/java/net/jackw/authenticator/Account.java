package net.jackw.authenticator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class Account {
	private CodeGenerator codeGenerator;
	private String issuer;
	private String username;
	private int id;
	private boolean hasImage = false;
	private Bitmap image = null;
	private Context context = null;

	public Account (CodeGenerator generator, String username, String issuer) {
		this(generator, username, issuer, 0);
	}
	public Account (CodeGenerator generator, String username, String issuer, int id) {
		this.codeGenerator = generator;
		this.username = username;
		this.issuer = issuer;
		this.id = id;
	}

	public CodeGenerator getCodeGenerator () {
		return codeGenerator;
	}
	public String getIssuer () {
		return issuer;
	}
	public String getUsername () {
		return username;
	}
	public int getId () {
		return id;
	}

	public void setId (int id) {
		this.id = id;
	}

	//<editor-fold desc="Image helper functions">
	public void hasImage (boolean val, Context context) {
		if (val && context == null) {
			throw new IllegalArgumentException("Context must not be null val is true");
		}
		hasImage = val;
		this.context = context;
	}
	public boolean hasImage () {
		return hasImage;
	}
	public Bitmap getImage () {
		if (!hasImage) {
			return null;
		}
		if (image == null) {
			try {
				FileInputStream file = context.openFileInput(generateImageFileName());
				image = BitmapFactory.decodeStream(file);
			} catch (FileNotFoundException | IllegalArgumentException e) {
				image = null;
			}
			if (image == null) {
				hasImage = false;
				this.save();
			}
		}
		return image;
	}

	private String generateImageFileName () {
		return "account_image_" + Integer.toString(this.id);
	}
	//</editor-fold>

	public void save () {
		DatabaseHelper.instance().getAccountsDb().saveAccount(this);
	}
}

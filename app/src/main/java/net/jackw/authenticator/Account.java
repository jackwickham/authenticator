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
	private Integer pos = null;
	private Context context = null;

	public Account (CodeGenerator generator, String username, String issuer) {
		this(generator, username, issuer, 0, null);
	}
	public Account (CodeGenerator generator, String username, String issuer, int id, Integer pos) {
		this.codeGenerator = generator;
		this.username = username;
		this.issuer = issuer;
		this.id = id;
		this.pos = pos;
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
	public Integer getPos () {
		return pos;
	}

	public void setId (int id) {
		this.id = id;
	}
	public void setPos (int pos) {
		// null can't be used as arg because the index must always exist
		if (pos <= 0) {
			throw new IllegalArgumentException("Pos must be a positive integer");
		}
		this.pos = pos;
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

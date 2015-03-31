package com.brashmonkey.spriter;

import java.util.HashMap;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.brashmonkey.spriter.Drawer;
import com.brashmonkey.spriter.Loader;
import com.brashmonkey.spriter.Timeline.Key.Object;
import com.sidereal.lumm.architecture.LummSceneLayer;
import com.sidereal.lumm.util.FloatWrapper;

public class LibGdxDrawer extends Drawer<Sprite>{
	
	private ShapeRenderer renderer;
	private Color tintColor;
	
	private float transparency;
	private Color targetColor;
	
	private Color realColor;
	private String currObjectName;
	public HashMap<String, Color> customObjectColor;
	public HashMap<String, FloatWrapper> customObjectTransparency;
	
	
	public LibGdxDrawer(Loader<Sprite> loader){
		super(loader);
		tintColor = new Color(Color.WHITE);
		transparency = 1;
		realColor = tintColor.cpy();
		customObjectColor = new HashMap<String, Color>();
		customObjectTransparency =  new HashMap<String, FloatWrapper>();
	}
	
	public LibGdxDrawer  setCustomObjectColor(String objectName, Color color)
	{
		if(objectName != null)
		{
			customObjectColor.put(objectName, color);
		}
		return this;
	}
	
	public LibGdxDrawer setCustomObjectTransparency(String objectName, FloatWrapper transparency)
	{
		if(objectName != null )
		{
			customObjectTransparency.put(objectName, transparency);
		}
		return this;
	}
	
	public void setTint(Color c)
	{
		tintColor.set(c);
	}
	
	@Override
	public void setColor(float r, float g, float b, float a) {
		renderer.setColor(r, g, b, a);
	}
	
	public void setTransparency(float transparency)
	{
		this.transparency = transparency;
	}
	
	@Override
	public void rectangle(float x, float y, float width, float height) {
		renderer.rect(x, y, width, height);
	}
	
	@Override
	public void line(float x1, float y1, float x2, float y2) {
		renderer.line(x1, y1, x2, y2);
	}

	@Override
	public void circle(float x, float y, float radius) {
		renderer.circle(x, y, radius);
	}

	@Override
	public void draw(Player player, Object object,LummSceneLayer batch) {
		Sprite sprite = loader.get(object.ref);
		float newPivotX = (sprite.getWidth() * object.pivot.x);
		float newX = object.position.x - newPivotX;
		float newPivotY = (sprite.getHeight() * object.pivot.y);
		float newY = object.position.y - newPivotY;
	
		currObjectName = player.getNameFor(object);
		float alpha = 0;
		
		
		if(customObjectColor.containsKey(currObjectName)
				&& customObjectColor.get(currObjectName) != null)
		{
			targetColor = customObjectColor.get(currObjectName);
		}
		else
		{
			targetColor = tintColor;
		}
		
		if(customObjectTransparency.containsKey(currObjectName) 
			&& customObjectTransparency.get(currObjectName) != null)
		{
			alpha = customObjectTransparency.get(currObjectName).get();
		}
		else
		{
			alpha = targetColor.a * object.alpha * transparency;
		}
		
		
		
		realColor = sprite.getColor();
		if(realColor.r != targetColor.r ||
			realColor.g != targetColor.g ||
			realColor.b != targetColor.b ||
			realColor.a != alpha)
		{
			sprite.setColor(targetColor.r, targetColor.g, targetColor.b, alpha);
		}
		
		sprite.setX(newX);
		sprite.setY(newY);
		
		sprite.setOrigin(newPivotX, newPivotY);
		sprite.setRotation(object.angle);
		sprite.setScale(object.scale.x, object.scale.y);
		 
		
		if(batch.renderingArea.overlaps(sprite.getBoundingRectangle()))
			sprite.draw(batch.spriteBatch);
	}
}

/*******************************************************************************
 * Copyright 2014 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.sidereal.lumm.components.renderer.scml;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.brashmonkey.spriter.LibGdxDrawer;
import com.brashmonkey.spriter.LibGdxLoader;
import com.brashmonkey.spriter.Player;
import com.brashmonkey.spriter.Entity.CharacterMap;
import com.sidereal.lumm.components.renderer.Drawer;
import com.sidereal.lumm.util.FloatWrapper;

/** Class for rendering images with a specific pattern in their rotation or position over a time span by extracting data from a
 * .scml file (made using Spriter). Created using {@link SCMLBuilder}.
 * 
 * @author Claudiu Bele */
public class SCMLDrawer extends Drawer {

	// region fields

	private Vector2 offsetPosition;
	private float animationTime;

	private LibGdxLoader loader;
	private LibGdxDrawer drawer;
	private Player player;
	private CharacterMap[] characterMaps;
	private CharacterMap[] internalCharacterMaps;

	// endregion fields

	// region Constructors

	public SCMLDrawer (com.sidereal.lumm.components.renderer.Renderer renderer, String name, String filepath,
		boolean useRawDelta) {

		super(renderer, name, useRawDelta);
		setPlayer(filepath);
	}

	// endregion Constructors

	// region methods

	@Override
	public void draw (float delta) {

		if (animationTime == -1)
			player.speed = (int)((delta * 1000) * (player.getAnimation().length / 1000f));
		else
			player.speed = (int)((delta * 1000) * (player.getAnimation().length / (animationTime * 1000f)));

		player.setPosition(offsetPosition.x + renderer.object.position.getX(), offsetPosition.y + renderer.object.position.getY());

		player.update();
		drawer.draw(player, renderer.object.getGameBatch());
	}

	@Override
	public void dispose () {

	}

	@Override
	protected boolean isOutOfBounds () {

		return false;
	}

	// region setters and getters

	public Vector2 getOffsetPosition () {

		return offsetPosition;
	}

	public void setOffsetPosition (float x, float y) {

		this.offsetPosition.set(x, y);
	}

	public void setColor (Color c) {

		drawer.setTint(c);
	}

	public void setTransparency (float x) {

		drawer.setTransparency(x);
	}

	public void setScale (float x) {

		player.setScale(x);
	}

	/** Sets the Spriter player to render. If will load the play with the first animation.
	 * 
	 * @param filepath */
	public void setPlayer (String filepath) {

		loader = SCMLBuilder.getAnimation(filepath);

		drawer = new LibGdxDrawer(loader);
		player = new Player(loader.data.getEntity(0));
		animationTime = player.getAnimation().length;
	}

	/** Sets the animation time. If you want to set the animation duration to the default one, use -1 as a parameter.
	 * 
	 * @param animationTime */
	public void setAnimationTime (float animationTime) {

		this.animationTime = animationTime;
	}

	/** Sets the animation to play, as named in the SCML project. This will reset the custom animation time if set to the default
	 * new animation's duration
	 * 
	 * @param animationName animation name */
	public void setAnimation (String animationName) {

		player.setAnimation(animationName);
		animationTime = player.getAnimation().length;

	}

	/** Sets the available character maps. All other character maps will be removed.
	 * 
	 * @param enableOnCall Whether to apply the character maps when the method finishes
	 * @param characterMaps the maps to load */
	public void setCharacterMaps (boolean enableOnCall, String... characterMaps) {

		if (characterMaps == null) {
			player.characterMaps = null;
			return;
		}
		this.internalCharacterMaps = new CharacterMap[characterMaps.length];
		this.characterMaps = new CharacterMap[characterMaps.length];
		for (int i = 0; i < characterMaps.length; i++) {
			this.internalCharacterMaps[i] = player.getEntity().getCharacterMap(characterMaps[i]);
			if (enableOnCall) this.characterMaps[i] = this.internalCharacterMaps[i];

		}
		player.characterMaps = (enableOnCall) ? this.characterMaps : null;
	}

	/** Sets the enabled character maps.
	 * <p>
	 * All of the character maps ( including the enabled ones ) will be disabled, afterwards the ones that are passed to the method
	 * will be enabled
	 * 
	 * @param characterMaps */
	public void setEnabledCharacterMaps (String... characterMaps) {

		// null parameter, disable all maps
		if (characterMaps == null) {
			player.characterMaps = null;
			return;
		}

		// set all character maps to null, then we enable all of them that we
		// want
		for (int i = 0; i < this.characterMaps.length; i++) {
			this.characterMaps[i] = null;
		}

		for (int i = 0; i < characterMaps.length; i++) {
			for (int j = 0; j < this.internalCharacterMaps.length; j++) {
				if (this.internalCharacterMaps[j].name.equals(characterMaps[i])) {
					this.characterMaps[j] = this.internalCharacterMaps[j];
				}
			}
		}
		player.characterMaps = this.characterMaps;
	}

	/** Enable or disable multiple character maps at a time.
	 * <p>
	 * As opposed to {@link #setEnabledCharacterMaps(String...)}, this method will not alter other methods enabled or disabled.
	 * 
	 * @param enable whether to enable or not
	 * @param characterMaps maps to enable or disable */
	public void enableCharacterMaps (boolean enable, String... characterMaps) {

		for (int i = 0; i < characterMaps.length; i++) {
			for (int j = 0; j < this.internalCharacterMaps.length; j++) {
				if (this.internalCharacterMaps[j].name.equals(characterMaps[i])) {
					this.characterMaps[j] = (enable) ? this.internalCharacterMaps[j] : null;
				}
			}
		}
		player.characterMaps = this.characterMaps;

	}

	/** Sets the color of an individual object that is part of a Spriter entity. The color will be persistent throughout animations
	 * if an object with the same name can be found in multiple animations
	 * 
	 * @param objectName name of the "part" or element in a spriter entity
	 * @param color the color to set it to */
	public void setObjectColor (String objectName, Color color) {
		drawer.setCustomObjectColor(objectName, color);
	}

	/** Sets the transparency of an individual object that is part of a Spriter entity. The transparency will be persistent
	 * throughout animations if an object with the same name can be found in multiple animations
	 * 
	 * @param objectName name of the "part" or element in a spriter entity
	 * @param transparency the transparency to set it to */
	public void setObjectTransparency (String objectName, float transparency) {
		drawer.setCustomObjectTransparency(objectName, new FloatWrapper(transparency));
	}

	// region default getters and setters

	public float getAnimationTime () {

		return animationTime;
	}

	public LibGdxLoader getLoader () {

		return loader;
	}

	public void setLoader (LibGdxLoader loader) {

		this.loader = loader;
	}

	public LibGdxDrawer getDrawer () {

		return drawer;
	}

	public void setDrawer (LibGdxDrawer drawer) {

		this.drawer = drawer;
	}

	public Player getPlayer () {

		return player;
	}

	// endregion

	// endregion

	// endregion methods

}

/*******************************************************************************
 * Copyright 2015 See AUTHORS file.
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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import com.brashmonkey.spriter.Data;
import com.brashmonkey.spriter.LibGdxDrawer;
import com.brashmonkey.spriter.LibGdxLoader;
import com.brashmonkey.spriter.SCMLReader;
import com.sidereal.lumm.architecture.Lumm;
import com.sidereal.lumm.architecture.LummObject;
import com.sidereal.lumm.components.renderer.DrawerBuilder;

/** {@link DrawerBuilder} for {@link SCMLDrawer} drawer objects.
 * 
 * @author Claudiu Bele. */
public class SCMLBuilder extends DrawerBuilder<SCMLDrawer> {

	// region static

	private static ObjectMap<String, LibGdxLoader> spriterLoaders;

	/** loads an animation from an .scml file at the specified location */
	public static void makeAnimation (String filepath) {

		if (spriterLoaders == null) spriterLoaders = new ObjectMap<String, LibGdxLoader>();
		if (spriterLoaders.containsKey(filepath)) return;

		final Data SCMLData;
		if (Lumm.assets.getFileType().equals(FileType.Internal))
			SCMLData = new SCMLReader(Gdx.files.internal(filepath).read()).getData();
		else
			SCMLData = new SCMLReader(Gdx.files.classpath(filepath).read()).getData();

		LibGdxLoader loader = new LibGdxLoader(SCMLData);
		loader.load(Gdx.files.internal(filepath).file());

		spriterLoaders.put(filepath, loader);
	}

	/** Retrieves an animation if it can be found, otherwise null
	 * 
	 * @param filepath path to the .scml file
	 * @return A {@link LibGdxLoader} if found, otherwise null. */
	public static LibGdxLoader getAnimation (String filepath) {

		if (spriterLoaders == null) spriterLoaders = new ObjectMap<String, LibGdxLoader>();

		if (!spriterLoaders.containsKey(filepath)) {
			makeAnimation(filepath);
			return spriterLoaders.get(filepath);
		}

		LibGdxLoader loader = spriterLoaders.get(filepath);
		return loader;
	}

	/** Unloads an animation from memory
	 * 
	 * @param filepath : path to the .scml file */
	public static void removeAnimation (String filepath) {

		if (spriterLoaders == null) {
			spriterLoaders = new ObjectMap<String, LibGdxLoader>();
			return;
		}
		if (!spriterLoaders.containsKey(filepath)) return;
		spriterLoaders.get(filepath).dispose();
	}

	public static class AnimationData {
		public LibGdxLoader loader;
		public LibGdxDrawer drawer;
	}

	// endregion utility/static

	// region fields

	/** Whether to enable character maps at the start of application. Set in {@link #setCharacterMaps}. */
	private boolean enableCharacterMapsAtStart;

	/** Character map to be applied to the entity. Is initially set to null. If it is null, no character map is used. */
	private String characterMaps[];

	/** Color used for tinting the animation */
	private Color tintColor;

	/** Transparency of the animation. {@link #tintColor}'s alpha as well as individual object alpha is also taken in consideration
	 * when determining the final alpha of an object in an animation. */
	private float transparency;

	/** The scale of the entire animation player */
	private float scale;

	/** Filepath to SCML file. */
	private String filepath;

	/** Offset between the rendering of the object and the {@link LummObject}. Used to set
	 * {@link SCMLDrawer#setOffsetPosition(Vector2)} */
	private float offsetX, offsetY;

	/** Describes the time of the animation. Used for setting {@link SCMLDrawer#setAnimationTime(float)} */
	private float animationTime;

	/** Describes the animation to play. It must be in the .scml file. Sets {@link SCMLDrawer#setAnimation(String)} */
	private String animation;

	// endregion fields

	// region constructors

	public SCMLBuilder (String spriterEntityName) {

		super();
		this.filepath = spriterEntityName;
		animationTime = -1;
		scale = 1;
		transparency = 1;
	}

	// endregion constructors

	// region methods

	@Override
	protected SCMLDrawer build (String name) {

		SCMLDrawer drawer = new SCMLDrawer(renderer, name, filepath, false);
		if (animation != null) drawer.setAnimation(animation);
		drawer.setOffsetPosition(offsetX, offsetY);
		if (tintColor != null) drawer.setColor(tintColor);
		drawer.setTransparency(transparency);
		drawer.setScale(scale);
		drawer.setAnimationTime(animationTime);
		drawer.setCharacterMaps(enableCharacterMapsAtStart, characterMaps);

		return drawer;
	}

	public SCMLBuilder setOffsetPosition (float x, float y) {

		this.offsetX = x;
		this.offsetY = y;
		return this;
	}

	public SCMLBuilder setAnimationTime (float animationTime) {

		this.animationTime = animationTime;
		return this;
	}

	public SCMLBuilder setAnimation (String animation) {

		this.animation = animation;
		return this;
	}

	public SCMLBuilder setTint (Color color) {

		this.tintColor = color;
		return this;
	}

	public SCMLBuilder setTransparency (float transparency) {

		this.transparency = transparency;
		return this;
	}

	public SCMLBuilder setScale (float scale) {

		this.scale = scale;
		return this;
	}

	/** Sets the character maps that will be applied to the SCML entity. In {@link #build(String)}, after building the drawer,
	 * {@link SCMLDrawer#setCharacterMap(String)} will be called.
	 * <p>
	 * The character map is a skin that can be applied to an animation.
	 * 
	 * @param maps The name of the character map, null can be used for the default one to be used, but is pointless as the default
	 *           map is selected by default. At runtime it might be necessary to change the map, which will be achieved using
	 *           {@link SCMLDrawer#setCharacterMap(String)}
	 * @param enableAtStart Whether to enable all of the character maps when the drawer is made.
	 * 
	 * @return */
	public SCMLBuilder setCharacterMaps (boolean enableAtStart, String... maps) {

		this.enableCharacterMapsAtStart = enableAtStart;
		if (maps == null)
			this.characterMaps = null;
		else
			this.characterMaps = maps;
		return this;
	}

	// endregion methods
}

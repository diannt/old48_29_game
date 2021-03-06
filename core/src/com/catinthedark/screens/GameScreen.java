package com.catinthedark.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.catinthedark.Constants;
import com.catinthedark.GameScore;
import com.catinthedark.InterceptionManager;
import com.catinthedark.assets.Assets;
import com.catinthedark.entities.Entity;
import com.catinthedark.hud.GameHud;
import com.catinthedark.level.Level;

/**
 * Created by Ilya on 26.04.2014.
 */
public class GameScreen extends Basic2DScreen {

	final GameHud hud;
	Level level;
	InterceptionManager interManager;
	final SpriteBatch batchMap;
	final OrthographicCamera backCamera;
	final int[] layers = new int[] { 0 };
	long soundId;

	public GameScreen(ScreenChain chain) {
		super(chain);

		backCamera = new OrthographicCamera(Constants.VIEW_PORT_WIDTH,
				Constants.VIEW_PORT_HEIGHT);

		batchMap = new SpriteBatch();

		this.hud = new GameHud();
		hud.conf().setX(10).setY(585);

	}
	
	@Override
	public void show() {
		
//		Assets.music.play();
//		Assets.music.setLooping(true);
				
		Gdx.input.setInputProcessor(this);
		level = new Level(this);
		interManager = new InterceptionManager(level);
		backCamera.position.set(new float[] { Constants.VIEW_PORT_WIDTH / 2,
				Constants.VIEW_PORT_HEIGHT / 2, 0 });
		camera.position.set(Constants.VIEW_PORT_WIDTH / 2f,
				Constants.VIEW_PORT_HEIGHT / 2f, 0);
		camera.update();
		backCamera.update();
		
		GameScore.getInstance().setDemocracyLevel(0);
		GameScore.getInstance().setHealth(100);
		GameScore.getInstance().resetScore();
		
		hud.setDemocracyLevel(0);
		hud.setHealth(100);
	}

	public Camera getCamera() {
		return camera;
	}

	@Override
	public void render(float delta) {
		super.render(delta);
		processKeys();
		interManager.manage();

		// draw background image
		Assets.backgroundRenderer.setView(backCamera);
		Assets.backgroundRenderer.render(layers);

		batchMap.setProjectionMatrix(camera.combined);

		batchMap.begin();
		level.render(delta, batchMap);
		batchMap.end();
		
		if(GameScore.getInstance().getHealth() == 0){
			//Assets.music.stop();
			next();
		}
		//win
		if(GameScore.getInstance().getDemocracyLevel() == Constants.DEMOCRACY_LEVEL_MAX){
			//Assets.music.stop();
			gotoFrame(8);
		}

		hud.setHealth(GameScore.getInstance().getHealth());
		hud.setDemocracyLevel(100 / Constants.DEMOCRACY_LEVEL_MAX
				* GameScore.getInstance().getDemocracyLevel());

		hud.render();
	}

    @Override
    public void dispose() {
        super.dispose();
        hud.dispose();
        batchMap.dispose();
    }

    public void processKeys() {
        Entity.State lastState = Entity.State.IDLE;

		if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
			level.placeOilFactory();
            level.president.move(Entity.State.IDLE, camera);
            return;
		}
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            lastState = Entity.State.AIM_DOWN;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) {
            lastState = Entity.State.AIM_UP;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.ENTER)) {
            level.shut(level.president);
            level.president.move(lastState, camera);
            return;
        }
        level.president.move(lastState, camera);

		if (Gdx.input.isKeyPressed(Input.Keys.D)
				|| Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
			level.president.direction = Entity.Direction.RIGHT;
			level.president.move(Entity.State.RUN, camera);

			if (needMoveCamera()) {
				moveMainCamera();
				moveBackCamera();
				level.president.move(Entity.State.RUN, camera);
			}
		} else if (Gdx.input.isKeyPressed(Input.Keys.A)
				|| Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
			level.president.direction = Entity.Direction.LEFT;
			level.president.move(Entity.State.RUN, camera);
		} else {

        }

		// FIXME: only for debug
		if (Gdx.input.isKeyPressed(Input.Keys.G))
			next();
	}

	private void moveBackCamera() {

		final int vpw = Constants.VIEW_PORT_WIDTH;

		float backCamPos = backCamera.position.x;
		backCamPos += Constants.backCameraSpeed.x;
		if (backCamPos >= vpw / 2.0f + 2 * vpw)
			backCamPos = vpw / 2.0f;

		backCamera.position.set(backCamPos, backCamera.position.y,
				backCamera.position.z);

		backCamera.update();
	}

	private void moveMainCamera() {
		camera.position.set(camera.position.x + Constants.mainCameraSpeed.x,
				camera.position.y, camera.position.z);
		camera.update();
	}

	private boolean needMoveCamera() {
		return camera.position.x - level.president.WIDTH
				- Constants.maxPresidentDestinationFromBorder <= level.president
					.getX();
	}
}

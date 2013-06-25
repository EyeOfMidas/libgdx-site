package com.badlogicgames.libgdx.site;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.FileUtils;

import com.badlogicgames.libgdx.site.db.FileGameDatabase;
import com.badlogicgames.libgdx.site.db.Game;
import com.badlogicgames.libgdx.site.db.GameDatabase;
import com.sun.jersey.spi.resource.Singleton;

/**
 * Simple service using sqllite to store and return
 * libgdx game entries, for libgdx's site.
 * @author badlogic
 */
@Path("/")
@Singleton
public class GameService {
	private final GameDatabase db;
	private final String recaptchaPrivateKey;
	
	public GameService() throws IOException {
		String dir = System.getenv().get("GDX_GAME_DB_DIR");
		if(dir == null) {
			dir = "/opt/gamedb";
		}
		recaptchaPrivateKey = FileUtils.readFileToString(new File(dir, "recaptcha.key")).trim();
		db = new FileGameDatabase(dir);
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("games")
	public List<Game> games() {
		return db.getGames();
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("game")
	public Game game(@QueryParam("id") String gameId) {
		return db.getGame(gameId);
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("randomGames")
	public List<Game> randomGames(@QueryParam("num") int num) {
		if(num == 0) num = 8;
		List<Game> games = db.getGames();
		Collections.shuffle(games);
		List<Game> randomGames = new ArrayList<Game>();
		for(int i = 0; i < num && i < games.size(); i++) {
			randomGames.add(games.get(i));
		}
		return randomGames;
	}
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("create")
	public Result create(@Context HttpServletRequest hsr, GameRequest request) {
		if(!checkCaptcha(hsr.getRemoteAddr(), request)) return new Result(false, "Captcha wrong");
		String token = db.create(request.game);
		return new Result(true, token);
	}
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("update")
	public Result update(@Context HttpServletRequest hsr, GameRequest request) {
		if(!checkCaptcha(hsr.getRemoteAddr(), request)) return new Result(false, "Captcha wrong");
		return new Result(db.update(request.token, request.game), "Updated");
	}
	
	private boolean checkCaptcha(String remoteIp, GameRequest request) {
		String privateKey = recaptchaPrivateKey;
		String challenge = request.challenge;
		String response = request.response;
		String result = HttpUtils.postHttp("http://www.google.com/recaptcha/api/verify", 
							"privatekey", privateKey,
							"remoteip", remoteIp,
							"challenge", challenge,
							"response", response);
		if(result == null) return false;
		System.out.println(result);
		return result.startsWith("true");
	}
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("delete")
	public Result delete(GameRequest request) {
		// FIXME
		return null;
//		return new Result(db.delete(request.token, request.game), "Deleted");
	}
}

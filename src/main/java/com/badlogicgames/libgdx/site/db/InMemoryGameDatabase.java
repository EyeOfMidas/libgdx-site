package com.badlogicgames.libgdx.site.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class InMemoryGameDatabase implements GameDatabase {
	Map<String, String> tokensToIds = new HashMap<String, String>();
	Map<String, Game> idsToGames = new HashMap<String, Game>();
	
	public InMemoryGameDatabase(Game[] games) {
		for(Game game: games) {
			idsToGames.put(game.id, game);
		}
	}
	
	public synchronized String create(Game game) {
		if(game == null) return null;
		game.id = UUID.randomUUID().toString();
		game.created = new Date();
		idsToGames.put(game.id, game);
		String token = UUID.randomUUID().toString();
		tokensToIds.put(token, game.id);
		return token;
	}

	public synchronized boolean update(String token, Game game) {
		if(token == null) return false;
		if(game == null) return false;
		if(game.id == null) return false;
		String id = tokensToIds.get(token);
		if(id == null) return false;
		if(!id.equals(game.id)) return false;
		idsToGames.put(id, game);
		return true;
	}

	public synchronized boolean delete(String token, String gameId) {
		if(token == null) return false;
		if(gameId == null) return false;
		String id = tokensToIds.get(token);
		if(id == null) return false;
		if(!id.equals(gameId)) return false;
		idsToGames.remove(id);
		tokensToIds.remove(token);
		return true;
	}

	public synchronized List<Game> getGames() {
		return new ArrayList<Game>(idsToGames.values());
	}
}

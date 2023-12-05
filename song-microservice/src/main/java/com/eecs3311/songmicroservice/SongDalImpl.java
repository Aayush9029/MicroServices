package com.eecs3311.songmicroservice;

import java.util.List;
import java.util.Random;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Sort;

@Repository
public class SongDalImpl implements SongDal {

	private final MongoTemplate db;

	@Autowired
	public SongDalImpl(MongoTemplate mongoTemplate) {
		this.db = mongoTemplate;
	}

	@Override
	public DbQueryStatus addSong(Song songToAdd) {
		try {
			Song song = db.insert(songToAdd);
			Utils.log("🧑‍💻 addSong called with songId: " + song.getId(), LogType.INFO);
			return new DbQueryStatus("Song added successfully", DbQueryExecResult.QUERY_OK);
		} catch (Exception e) {
			Utils.log("🧑‍💻 addSong called with songId: " + songToAdd.getId() + " failed", LogType.ERROR);
			return new DbQueryStatus("Error adding song: " + e.getMessage(), DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
	}

	@Override
	public DbQueryStatus findSongById(String songId) {
		try {
			Song song = db.findById(new ObjectId(songId), Song.class);
			Utils.log("🧑‍💻 findSongById called with songId: " + songId, LogType.INFO);
			if (song == null) {
				Utils.log("🧑‍💻 findSongById called with songId: " + songId + " failed", LogType.ERROR);
				return new DbQueryStatus("Song not found", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
			}
			DbQueryStatus status = new DbQueryStatus("Song found", DbQueryExecResult.QUERY_OK);
			status.setData(song);
			return status;
		} catch (Exception e) {
			Utils.log("🧑‍💻 findSongById called with songId: " + songId + " failed", LogType.ERROR);
			return new DbQueryStatus("Error finding song: " + e.getMessage(), DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
	}

	@Override
	public DbQueryStatus getSongTitleById(String songId) {
		try {
			Song song = db.findById(new ObjectId(songId), Song.class);
			if (song == null) {
				Utils.log("🎵 Song Not Found", LogType.WARNING);
				return new DbQueryStatus("Song not found", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
			}
			DbQueryStatus status = new DbQueryStatus("Song title found", DbQueryExecResult.QUERY_OK);
			status.setData(song.getSongName());
			return status;
		} catch (Exception e) {
			Utils.log("🧑‍💻 getSongTitleById called with songId: " + songId + " failed", LogType.ERROR);
			return new DbQueryStatus("Error finding song title: " + e.getMessage(),
					DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
	}

	@Override
	public DbQueryStatus deleteSongById(String songId) {
		try {
			Query query = new Query(Criteria.where("_id").is(new ObjectId(songId)));
			db.remove(query, Song.class);
			Utils.log("🧑‍💻 deleteSongById called with songId: " + songId, LogType.INFO);
			return new DbQueryStatus("Song deleted successfully", DbQueryExecResult.QUERY_OK);
		} catch (Exception e) {
			Utils.log("🧑‍💻 deleteSongById called with songId: " + songId + " failed", LogType.ERROR);
			return new DbQueryStatus("Error deleting song: " + e.getMessage(), DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
	}

	@Override
	public DbQueryStatus updateSongFavouritesCount(String songId, boolean shouldDecrement) {
		try {
			Query query = new Query(Criteria.where("_id").is(new ObjectId(songId)));
			Update update = new Update();
			if (shouldDecrement) {
				update.inc("songAmountFavourites", -1);
			} else {
				update.inc("songAmountFavourites", 1);
			}
			db.findAndModify(query, update, Song.class);
			Utils.log("🧑‍💻 updateSongFavouritesCount called with songId: " + songId, LogType.INFO);
			return new DbQueryStatus("Song favourites count updated successfully", DbQueryExecResult.QUERY_OK);
		} catch (Exception e) {
			Utils.log("🧑‍💻 updateSongFavouritesCount called with songId: " + songId + " failed", LogType.ERROR);
			return new DbQueryStatus("Error updating song favourites count: " + e.getMessage(),
					DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
	}

	@Override
	public DbQueryStatus findTrendingSongs(Integer limit) {
		try {
			Query query = new Query();
			query.with(Sort.by(Sort.Direction.DESC, "songAmountFavourites"));

			if (limit != null && limit > 0) {
				query.limit(limit);
			}

			List<Song> songs = db.find(query, Song.class);
			Utils.log("🧑‍💻 findTrendingSongs called", LogType.INFO);

			if (songs.isEmpty()) {
				return new DbQueryStatus("No trending songs found", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
			}

			DbQueryStatus dbQueryStatus = new DbQueryStatus("Trending songs found", DbQueryExecResult.QUERY_OK);
			dbQueryStatus.setData(songs);
			return dbQueryStatus;
		} catch (Exception e) {
			Utils.log("🧑‍💻 findTrendingSongs failed: " + e.getMessage(), LogType.ERROR);
			return new DbQueryStatus("Error finding trending songs: " + e.getMessage(),
					DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
	}

	/*
	 * Note: This feature isn't ideal but is a great starting point for a more
	 * complex implementation
	 * Implmenting this feature will require a lot of work and is not a priority for
	 * this project
	 * Requiring us to change the underlying data model and add a lot of complexity
	 * to the code
	 */
	@Override
	public DbQueryStatus getMadeForYouPlaylist() {
		try {
			Query query = new Query();
			query.limit(new Random().nextInt(10) + 1); // Randomly select up to 10 songs for the playlist
			List<Song> songs = db.find(query, Song.class);

			if (songs.isEmpty()) {
				return new DbQueryStatus("No songs found for the playlist", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
			}

			DbQueryStatus dbQueryStatus = new DbQueryStatus("Made for You Playlist generated successfully",
					DbQueryExecResult.QUERY_OK);
			dbQueryStatus.setData(songs);
			return dbQueryStatus;
		} catch (Exception e) {
			return new DbQueryStatus("Error generating Made for You Playlist: " + e.getMessage(),
					DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
	}

}
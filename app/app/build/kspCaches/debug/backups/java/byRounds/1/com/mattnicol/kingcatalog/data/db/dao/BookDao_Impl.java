package com.mattnicol.kingcatalog.data.db.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.mattnicol.kingcatalog.data.db.converter.Converters;
import com.mattnicol.kingcatalog.data.db.entity.BookEntity;
import com.mattnicol.kingcatalog.data.model.Adaptation;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Float;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class BookDao_Impl implements BookDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<BookEntity> __insertionAdapterOfBookEntity;

  private final Converters __converters = new Converters();

  private final EntityDeletionOrUpdateAdapter<BookEntity> __updateAdapterOfBookEntity;

  public BookDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfBookEntity = new EntityInsertionAdapter<BookEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR IGNORE INTO `books` (`id`,`title`,`author`,`as_bachman`,`year`,`decade`,`word_count`,`audible_minutes`,`story_type`,`keywords`,`genres`,`is_collection_parent`,`collection`,`collection_id`,`child_ids`,`has_adaptation`,`adaptations`,`imdb_url`,`cover_local_path`,`cover_candidate_url`,`goodreads_rating`,`goodreads_ratings_count`,`is_owned`,`is_read`,`is_reading_now`,`is_on_reading_list`,`last_status_changed`,`notes`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final BookEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getTitle());
        statement.bindString(3, entity.getAuthor());
        final int _tmp = entity.getAsBachman() ? 1 : 0;
        statement.bindLong(4, _tmp);
        if (entity.getYear() == null) {
          statement.bindNull(5);
        } else {
          statement.bindLong(5, entity.getYear());
        }
        if (entity.getDecade() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getDecade());
        }
        if (entity.getWordCount() == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, entity.getWordCount());
        }
        if (entity.getAudibleMinutes() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getAudibleMinutes());
        }
        statement.bindString(9, entity.getStoryType());
        final String _tmp_1 = __converters.toStringList(entity.getKeywords());
        statement.bindString(10, _tmp_1);
        final String _tmp_2 = __converters.toStringList(entity.getGenres());
        statement.bindString(11, _tmp_2);
        final int _tmp_3 = entity.isCollectionParent() ? 1 : 0;
        statement.bindLong(12, _tmp_3);
        if (entity.getCollection() == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, entity.getCollection());
        }
        if (entity.getCollectionId() == null) {
          statement.bindNull(14);
        } else {
          statement.bindLong(14, entity.getCollectionId());
        }
        final String _tmp_4 = __converters.toIntList(entity.getChildIds());
        statement.bindString(15, _tmp_4);
        final int _tmp_5 = entity.getHasAdaptation() ? 1 : 0;
        statement.bindLong(16, _tmp_5);
        final String _tmp_6 = __converters.toAdaptationList(entity.getAdaptations());
        statement.bindString(17, _tmp_6);
        if (entity.getImdbUrl() == null) {
          statement.bindNull(18);
        } else {
          statement.bindString(18, entity.getImdbUrl());
        }
        if (entity.getCoverLocalPath() == null) {
          statement.bindNull(19);
        } else {
          statement.bindString(19, entity.getCoverLocalPath());
        }
        if (entity.getCoverCandidateUrl() == null) {
          statement.bindNull(20);
        } else {
          statement.bindString(20, entity.getCoverCandidateUrl());
        }
        if (entity.getGoodreadsRating() == null) {
          statement.bindNull(21);
        } else {
          statement.bindDouble(21, entity.getGoodreadsRating());
        }
        if (entity.getGoodreadsRatingsCount() == null) {
          statement.bindNull(22);
        } else {
          statement.bindLong(22, entity.getGoodreadsRatingsCount());
        }
        final int _tmp_7 = entity.isOwned() ? 1 : 0;
        statement.bindLong(23, _tmp_7);
        final int _tmp_8 = entity.isRead() ? 1 : 0;
        statement.bindLong(24, _tmp_8);
        final int _tmp_9 = entity.isReadingNow() ? 1 : 0;
        statement.bindLong(25, _tmp_9);
        final int _tmp_10 = entity.isOnReadingList() ? 1 : 0;
        statement.bindLong(26, _tmp_10);
        if (entity.getLastStatusChanged() == null) {
          statement.bindNull(27);
        } else {
          statement.bindLong(27, entity.getLastStatusChanged());
        }
        if (entity.getNotes() == null) {
          statement.bindNull(28);
        } else {
          statement.bindString(28, entity.getNotes());
        }
      }
    };
    this.__updateAdapterOfBookEntity = new EntityDeletionOrUpdateAdapter<BookEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `books` SET `id` = ?,`title` = ?,`author` = ?,`as_bachman` = ?,`year` = ?,`decade` = ?,`word_count` = ?,`audible_minutes` = ?,`story_type` = ?,`keywords` = ?,`genres` = ?,`is_collection_parent` = ?,`collection` = ?,`collection_id` = ?,`child_ids` = ?,`has_adaptation` = ?,`adaptations` = ?,`imdb_url` = ?,`cover_local_path` = ?,`cover_candidate_url` = ?,`goodreads_rating` = ?,`goodreads_ratings_count` = ?,`is_owned` = ?,`is_read` = ?,`is_reading_now` = ?,`is_on_reading_list` = ?,`last_status_changed` = ?,`notes` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final BookEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getTitle());
        statement.bindString(3, entity.getAuthor());
        final int _tmp = entity.getAsBachman() ? 1 : 0;
        statement.bindLong(4, _tmp);
        if (entity.getYear() == null) {
          statement.bindNull(5);
        } else {
          statement.bindLong(5, entity.getYear());
        }
        if (entity.getDecade() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getDecade());
        }
        if (entity.getWordCount() == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, entity.getWordCount());
        }
        if (entity.getAudibleMinutes() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getAudibleMinutes());
        }
        statement.bindString(9, entity.getStoryType());
        final String _tmp_1 = __converters.toStringList(entity.getKeywords());
        statement.bindString(10, _tmp_1);
        final String _tmp_2 = __converters.toStringList(entity.getGenres());
        statement.bindString(11, _tmp_2);
        final int _tmp_3 = entity.isCollectionParent() ? 1 : 0;
        statement.bindLong(12, _tmp_3);
        if (entity.getCollection() == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, entity.getCollection());
        }
        if (entity.getCollectionId() == null) {
          statement.bindNull(14);
        } else {
          statement.bindLong(14, entity.getCollectionId());
        }
        final String _tmp_4 = __converters.toIntList(entity.getChildIds());
        statement.bindString(15, _tmp_4);
        final int _tmp_5 = entity.getHasAdaptation() ? 1 : 0;
        statement.bindLong(16, _tmp_5);
        final String _tmp_6 = __converters.toAdaptationList(entity.getAdaptations());
        statement.bindString(17, _tmp_6);
        if (entity.getImdbUrl() == null) {
          statement.bindNull(18);
        } else {
          statement.bindString(18, entity.getImdbUrl());
        }
        if (entity.getCoverLocalPath() == null) {
          statement.bindNull(19);
        } else {
          statement.bindString(19, entity.getCoverLocalPath());
        }
        if (entity.getCoverCandidateUrl() == null) {
          statement.bindNull(20);
        } else {
          statement.bindString(20, entity.getCoverCandidateUrl());
        }
        if (entity.getGoodreadsRating() == null) {
          statement.bindNull(21);
        } else {
          statement.bindDouble(21, entity.getGoodreadsRating());
        }
        if (entity.getGoodreadsRatingsCount() == null) {
          statement.bindNull(22);
        } else {
          statement.bindLong(22, entity.getGoodreadsRatingsCount());
        }
        final int _tmp_7 = entity.isOwned() ? 1 : 0;
        statement.bindLong(23, _tmp_7);
        final int _tmp_8 = entity.isRead() ? 1 : 0;
        statement.bindLong(24, _tmp_8);
        final int _tmp_9 = entity.isReadingNow() ? 1 : 0;
        statement.bindLong(25, _tmp_9);
        final int _tmp_10 = entity.isOnReadingList() ? 1 : 0;
        statement.bindLong(26, _tmp_10);
        if (entity.getLastStatusChanged() == null) {
          statement.bindNull(27);
        } else {
          statement.bindLong(27, entity.getLastStatusChanged());
        }
        if (entity.getNotes() == null) {
          statement.bindNull(28);
        } else {
          statement.bindString(28, entity.getNotes());
        }
        statement.bindLong(29, entity.getId());
      }
    };
  }

  @Override
  public Object insertAll(final List<BookEntity> books,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfBookEntity.insert(books);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final BookEntity book, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfBookEntity.handle(book);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object getById(final int id, final Continuation<? super BookEntity> $completion) {
    final String _sql = "SELECT * FROM books WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<BookEntity>() {
      @Override
      @Nullable
      public BookEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfAuthor = CursorUtil.getColumnIndexOrThrow(_cursor, "author");
          final int _cursorIndexOfAsBachman = CursorUtil.getColumnIndexOrThrow(_cursor, "as_bachman");
          final int _cursorIndexOfYear = CursorUtil.getColumnIndexOrThrow(_cursor, "year");
          final int _cursorIndexOfDecade = CursorUtil.getColumnIndexOrThrow(_cursor, "decade");
          final int _cursorIndexOfWordCount = CursorUtil.getColumnIndexOrThrow(_cursor, "word_count");
          final int _cursorIndexOfAudibleMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "audible_minutes");
          final int _cursorIndexOfStoryType = CursorUtil.getColumnIndexOrThrow(_cursor, "story_type");
          final int _cursorIndexOfKeywords = CursorUtil.getColumnIndexOrThrow(_cursor, "keywords");
          final int _cursorIndexOfGenres = CursorUtil.getColumnIndexOrThrow(_cursor, "genres");
          final int _cursorIndexOfIsCollectionParent = CursorUtil.getColumnIndexOrThrow(_cursor, "is_collection_parent");
          final int _cursorIndexOfCollection = CursorUtil.getColumnIndexOrThrow(_cursor, "collection");
          final int _cursorIndexOfCollectionId = CursorUtil.getColumnIndexOrThrow(_cursor, "collection_id");
          final int _cursorIndexOfChildIds = CursorUtil.getColumnIndexOrThrow(_cursor, "child_ids");
          final int _cursorIndexOfHasAdaptation = CursorUtil.getColumnIndexOrThrow(_cursor, "has_adaptation");
          final int _cursorIndexOfAdaptations = CursorUtil.getColumnIndexOrThrow(_cursor, "adaptations");
          final int _cursorIndexOfImdbUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "imdb_url");
          final int _cursorIndexOfCoverLocalPath = CursorUtil.getColumnIndexOrThrow(_cursor, "cover_local_path");
          final int _cursorIndexOfCoverCandidateUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "cover_candidate_url");
          final int _cursorIndexOfGoodreadsRating = CursorUtil.getColumnIndexOrThrow(_cursor, "goodreads_rating");
          final int _cursorIndexOfGoodreadsRatingsCount = CursorUtil.getColumnIndexOrThrow(_cursor, "goodreads_ratings_count");
          final int _cursorIndexOfIsOwned = CursorUtil.getColumnIndexOrThrow(_cursor, "is_owned");
          final int _cursorIndexOfIsRead = CursorUtil.getColumnIndexOrThrow(_cursor, "is_read");
          final int _cursorIndexOfIsReadingNow = CursorUtil.getColumnIndexOrThrow(_cursor, "is_reading_now");
          final int _cursorIndexOfIsOnReadingList = CursorUtil.getColumnIndexOrThrow(_cursor, "is_on_reading_list");
          final int _cursorIndexOfLastStatusChanged = CursorUtil.getColumnIndexOrThrow(_cursor, "last_status_changed");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final BookEntity _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpAuthor;
            _tmpAuthor = _cursor.getString(_cursorIndexOfAuthor);
            final boolean _tmpAsBachman;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfAsBachman);
            _tmpAsBachman = _tmp != 0;
            final Integer _tmpYear;
            if (_cursor.isNull(_cursorIndexOfYear)) {
              _tmpYear = null;
            } else {
              _tmpYear = _cursor.getInt(_cursorIndexOfYear);
            }
            final Integer _tmpDecade;
            if (_cursor.isNull(_cursorIndexOfDecade)) {
              _tmpDecade = null;
            } else {
              _tmpDecade = _cursor.getInt(_cursorIndexOfDecade);
            }
            final Integer _tmpWordCount;
            if (_cursor.isNull(_cursorIndexOfWordCount)) {
              _tmpWordCount = null;
            } else {
              _tmpWordCount = _cursor.getInt(_cursorIndexOfWordCount);
            }
            final Integer _tmpAudibleMinutes;
            if (_cursor.isNull(_cursorIndexOfAudibleMinutes)) {
              _tmpAudibleMinutes = null;
            } else {
              _tmpAudibleMinutes = _cursor.getInt(_cursorIndexOfAudibleMinutes);
            }
            final String _tmpStoryType;
            _tmpStoryType = _cursor.getString(_cursorIndexOfStoryType);
            final List<String> _tmpKeywords;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfKeywords);
            _tmpKeywords = __converters.fromStringList(_tmp_1);
            final List<String> _tmpGenres;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfGenres);
            _tmpGenres = __converters.fromStringList(_tmp_2);
            final boolean _tmpIsCollectionParent;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsCollectionParent);
            _tmpIsCollectionParent = _tmp_3 != 0;
            final String _tmpCollection;
            if (_cursor.isNull(_cursorIndexOfCollection)) {
              _tmpCollection = null;
            } else {
              _tmpCollection = _cursor.getString(_cursorIndexOfCollection);
            }
            final Integer _tmpCollectionId;
            if (_cursor.isNull(_cursorIndexOfCollectionId)) {
              _tmpCollectionId = null;
            } else {
              _tmpCollectionId = _cursor.getInt(_cursorIndexOfCollectionId);
            }
            final List<Integer> _tmpChildIds;
            final String _tmp_4;
            _tmp_4 = _cursor.getString(_cursorIndexOfChildIds);
            _tmpChildIds = __converters.fromIntList(_tmp_4);
            final boolean _tmpHasAdaptation;
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfHasAdaptation);
            _tmpHasAdaptation = _tmp_5 != 0;
            final List<Adaptation> _tmpAdaptations;
            final String _tmp_6;
            _tmp_6 = _cursor.getString(_cursorIndexOfAdaptations);
            _tmpAdaptations = __converters.fromAdaptationList(_tmp_6);
            final String _tmpImdbUrl;
            if (_cursor.isNull(_cursorIndexOfImdbUrl)) {
              _tmpImdbUrl = null;
            } else {
              _tmpImdbUrl = _cursor.getString(_cursorIndexOfImdbUrl);
            }
            final String _tmpCoverLocalPath;
            if (_cursor.isNull(_cursorIndexOfCoverLocalPath)) {
              _tmpCoverLocalPath = null;
            } else {
              _tmpCoverLocalPath = _cursor.getString(_cursorIndexOfCoverLocalPath);
            }
            final String _tmpCoverCandidateUrl;
            if (_cursor.isNull(_cursorIndexOfCoverCandidateUrl)) {
              _tmpCoverCandidateUrl = null;
            } else {
              _tmpCoverCandidateUrl = _cursor.getString(_cursorIndexOfCoverCandidateUrl);
            }
            final Float _tmpGoodreadsRating;
            if (_cursor.isNull(_cursorIndexOfGoodreadsRating)) {
              _tmpGoodreadsRating = null;
            } else {
              _tmpGoodreadsRating = _cursor.getFloat(_cursorIndexOfGoodreadsRating);
            }
            final Integer _tmpGoodreadsRatingsCount;
            if (_cursor.isNull(_cursorIndexOfGoodreadsRatingsCount)) {
              _tmpGoodreadsRatingsCount = null;
            } else {
              _tmpGoodreadsRatingsCount = _cursor.getInt(_cursorIndexOfGoodreadsRatingsCount);
            }
            final boolean _tmpIsOwned;
            final int _tmp_7;
            _tmp_7 = _cursor.getInt(_cursorIndexOfIsOwned);
            _tmpIsOwned = _tmp_7 != 0;
            final boolean _tmpIsRead;
            final int _tmp_8;
            _tmp_8 = _cursor.getInt(_cursorIndexOfIsRead);
            _tmpIsRead = _tmp_8 != 0;
            final boolean _tmpIsReadingNow;
            final int _tmp_9;
            _tmp_9 = _cursor.getInt(_cursorIndexOfIsReadingNow);
            _tmpIsReadingNow = _tmp_9 != 0;
            final boolean _tmpIsOnReadingList;
            final int _tmp_10;
            _tmp_10 = _cursor.getInt(_cursorIndexOfIsOnReadingList);
            _tmpIsOnReadingList = _tmp_10 != 0;
            final Long _tmpLastStatusChanged;
            if (_cursor.isNull(_cursorIndexOfLastStatusChanged)) {
              _tmpLastStatusChanged = null;
            } else {
              _tmpLastStatusChanged = _cursor.getLong(_cursorIndexOfLastStatusChanged);
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            _result = new BookEntity(_tmpId,_tmpTitle,_tmpAuthor,_tmpAsBachman,_tmpYear,_tmpDecade,_tmpWordCount,_tmpAudibleMinutes,_tmpStoryType,_tmpKeywords,_tmpGenres,_tmpIsCollectionParent,_tmpCollection,_tmpCollectionId,_tmpChildIds,_tmpHasAdaptation,_tmpAdaptations,_tmpImdbUrl,_tmpCoverLocalPath,_tmpCoverCandidateUrl,_tmpGoodreadsRating,_tmpGoodreadsRatingsCount,_tmpIsOwned,_tmpIsRead,_tmpIsReadingNow,_tmpIsOnReadingList,_tmpLastStatusChanged,_tmpNotes);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<BookEntity>> observeAll() {
    final String _sql = "SELECT * FROM books ORDER BY year ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"books"}, new Callable<List<BookEntity>>() {
      @Override
      @NonNull
      public List<BookEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfAuthor = CursorUtil.getColumnIndexOrThrow(_cursor, "author");
          final int _cursorIndexOfAsBachman = CursorUtil.getColumnIndexOrThrow(_cursor, "as_bachman");
          final int _cursorIndexOfYear = CursorUtil.getColumnIndexOrThrow(_cursor, "year");
          final int _cursorIndexOfDecade = CursorUtil.getColumnIndexOrThrow(_cursor, "decade");
          final int _cursorIndexOfWordCount = CursorUtil.getColumnIndexOrThrow(_cursor, "word_count");
          final int _cursorIndexOfAudibleMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "audible_minutes");
          final int _cursorIndexOfStoryType = CursorUtil.getColumnIndexOrThrow(_cursor, "story_type");
          final int _cursorIndexOfKeywords = CursorUtil.getColumnIndexOrThrow(_cursor, "keywords");
          final int _cursorIndexOfGenres = CursorUtil.getColumnIndexOrThrow(_cursor, "genres");
          final int _cursorIndexOfIsCollectionParent = CursorUtil.getColumnIndexOrThrow(_cursor, "is_collection_parent");
          final int _cursorIndexOfCollection = CursorUtil.getColumnIndexOrThrow(_cursor, "collection");
          final int _cursorIndexOfCollectionId = CursorUtil.getColumnIndexOrThrow(_cursor, "collection_id");
          final int _cursorIndexOfChildIds = CursorUtil.getColumnIndexOrThrow(_cursor, "child_ids");
          final int _cursorIndexOfHasAdaptation = CursorUtil.getColumnIndexOrThrow(_cursor, "has_adaptation");
          final int _cursorIndexOfAdaptations = CursorUtil.getColumnIndexOrThrow(_cursor, "adaptations");
          final int _cursorIndexOfImdbUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "imdb_url");
          final int _cursorIndexOfCoverLocalPath = CursorUtil.getColumnIndexOrThrow(_cursor, "cover_local_path");
          final int _cursorIndexOfCoverCandidateUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "cover_candidate_url");
          final int _cursorIndexOfGoodreadsRating = CursorUtil.getColumnIndexOrThrow(_cursor, "goodreads_rating");
          final int _cursorIndexOfGoodreadsRatingsCount = CursorUtil.getColumnIndexOrThrow(_cursor, "goodreads_ratings_count");
          final int _cursorIndexOfIsOwned = CursorUtil.getColumnIndexOrThrow(_cursor, "is_owned");
          final int _cursorIndexOfIsRead = CursorUtil.getColumnIndexOrThrow(_cursor, "is_read");
          final int _cursorIndexOfIsReadingNow = CursorUtil.getColumnIndexOrThrow(_cursor, "is_reading_now");
          final int _cursorIndexOfIsOnReadingList = CursorUtil.getColumnIndexOrThrow(_cursor, "is_on_reading_list");
          final int _cursorIndexOfLastStatusChanged = CursorUtil.getColumnIndexOrThrow(_cursor, "last_status_changed");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final List<BookEntity> _result = new ArrayList<BookEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final BookEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpAuthor;
            _tmpAuthor = _cursor.getString(_cursorIndexOfAuthor);
            final boolean _tmpAsBachman;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfAsBachman);
            _tmpAsBachman = _tmp != 0;
            final Integer _tmpYear;
            if (_cursor.isNull(_cursorIndexOfYear)) {
              _tmpYear = null;
            } else {
              _tmpYear = _cursor.getInt(_cursorIndexOfYear);
            }
            final Integer _tmpDecade;
            if (_cursor.isNull(_cursorIndexOfDecade)) {
              _tmpDecade = null;
            } else {
              _tmpDecade = _cursor.getInt(_cursorIndexOfDecade);
            }
            final Integer _tmpWordCount;
            if (_cursor.isNull(_cursorIndexOfWordCount)) {
              _tmpWordCount = null;
            } else {
              _tmpWordCount = _cursor.getInt(_cursorIndexOfWordCount);
            }
            final Integer _tmpAudibleMinutes;
            if (_cursor.isNull(_cursorIndexOfAudibleMinutes)) {
              _tmpAudibleMinutes = null;
            } else {
              _tmpAudibleMinutes = _cursor.getInt(_cursorIndexOfAudibleMinutes);
            }
            final String _tmpStoryType;
            _tmpStoryType = _cursor.getString(_cursorIndexOfStoryType);
            final List<String> _tmpKeywords;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfKeywords);
            _tmpKeywords = __converters.fromStringList(_tmp_1);
            final List<String> _tmpGenres;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfGenres);
            _tmpGenres = __converters.fromStringList(_tmp_2);
            final boolean _tmpIsCollectionParent;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsCollectionParent);
            _tmpIsCollectionParent = _tmp_3 != 0;
            final String _tmpCollection;
            if (_cursor.isNull(_cursorIndexOfCollection)) {
              _tmpCollection = null;
            } else {
              _tmpCollection = _cursor.getString(_cursorIndexOfCollection);
            }
            final Integer _tmpCollectionId;
            if (_cursor.isNull(_cursorIndexOfCollectionId)) {
              _tmpCollectionId = null;
            } else {
              _tmpCollectionId = _cursor.getInt(_cursorIndexOfCollectionId);
            }
            final List<Integer> _tmpChildIds;
            final String _tmp_4;
            _tmp_4 = _cursor.getString(_cursorIndexOfChildIds);
            _tmpChildIds = __converters.fromIntList(_tmp_4);
            final boolean _tmpHasAdaptation;
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfHasAdaptation);
            _tmpHasAdaptation = _tmp_5 != 0;
            final List<Adaptation> _tmpAdaptations;
            final String _tmp_6;
            _tmp_6 = _cursor.getString(_cursorIndexOfAdaptations);
            _tmpAdaptations = __converters.fromAdaptationList(_tmp_6);
            final String _tmpImdbUrl;
            if (_cursor.isNull(_cursorIndexOfImdbUrl)) {
              _tmpImdbUrl = null;
            } else {
              _tmpImdbUrl = _cursor.getString(_cursorIndexOfImdbUrl);
            }
            final String _tmpCoverLocalPath;
            if (_cursor.isNull(_cursorIndexOfCoverLocalPath)) {
              _tmpCoverLocalPath = null;
            } else {
              _tmpCoverLocalPath = _cursor.getString(_cursorIndexOfCoverLocalPath);
            }
            final String _tmpCoverCandidateUrl;
            if (_cursor.isNull(_cursorIndexOfCoverCandidateUrl)) {
              _tmpCoverCandidateUrl = null;
            } else {
              _tmpCoverCandidateUrl = _cursor.getString(_cursorIndexOfCoverCandidateUrl);
            }
            final Float _tmpGoodreadsRating;
            if (_cursor.isNull(_cursorIndexOfGoodreadsRating)) {
              _tmpGoodreadsRating = null;
            } else {
              _tmpGoodreadsRating = _cursor.getFloat(_cursorIndexOfGoodreadsRating);
            }
            final Integer _tmpGoodreadsRatingsCount;
            if (_cursor.isNull(_cursorIndexOfGoodreadsRatingsCount)) {
              _tmpGoodreadsRatingsCount = null;
            } else {
              _tmpGoodreadsRatingsCount = _cursor.getInt(_cursorIndexOfGoodreadsRatingsCount);
            }
            final boolean _tmpIsOwned;
            final int _tmp_7;
            _tmp_7 = _cursor.getInt(_cursorIndexOfIsOwned);
            _tmpIsOwned = _tmp_7 != 0;
            final boolean _tmpIsRead;
            final int _tmp_8;
            _tmp_8 = _cursor.getInt(_cursorIndexOfIsRead);
            _tmpIsRead = _tmp_8 != 0;
            final boolean _tmpIsReadingNow;
            final int _tmp_9;
            _tmp_9 = _cursor.getInt(_cursorIndexOfIsReadingNow);
            _tmpIsReadingNow = _tmp_9 != 0;
            final boolean _tmpIsOnReadingList;
            final int _tmp_10;
            _tmp_10 = _cursor.getInt(_cursorIndexOfIsOnReadingList);
            _tmpIsOnReadingList = _tmp_10 != 0;
            final Long _tmpLastStatusChanged;
            if (_cursor.isNull(_cursorIndexOfLastStatusChanged)) {
              _tmpLastStatusChanged = null;
            } else {
              _tmpLastStatusChanged = _cursor.getLong(_cursorIndexOfLastStatusChanged);
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            _item = new BookEntity(_tmpId,_tmpTitle,_tmpAuthor,_tmpAsBachman,_tmpYear,_tmpDecade,_tmpWordCount,_tmpAudibleMinutes,_tmpStoryType,_tmpKeywords,_tmpGenres,_tmpIsCollectionParent,_tmpCollection,_tmpCollectionId,_tmpChildIds,_tmpHasAdaptation,_tmpAdaptations,_tmpImdbUrl,_tmpCoverLocalPath,_tmpCoverCandidateUrl,_tmpGoodreadsRating,_tmpGoodreadsRatingsCount,_tmpIsOwned,_tmpIsRead,_tmpIsReadingNow,_tmpIsOnReadingList,_tmpLastStatusChanged,_tmpNotes);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<BookEntity>> observeBrowseable() {
    final String _sql = "\n"
            + "        SELECT * FROM books\n"
            + "        WHERE author IN ('Stephen King', 'Richard Bachman')\n"
            + "          AND (is_collection_parent = 1 OR collection_id IS NULL)\n"
            + "        ORDER BY year ASC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"books"}, new Callable<List<BookEntity>>() {
      @Override
      @NonNull
      public List<BookEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfAuthor = CursorUtil.getColumnIndexOrThrow(_cursor, "author");
          final int _cursorIndexOfAsBachman = CursorUtil.getColumnIndexOrThrow(_cursor, "as_bachman");
          final int _cursorIndexOfYear = CursorUtil.getColumnIndexOrThrow(_cursor, "year");
          final int _cursorIndexOfDecade = CursorUtil.getColumnIndexOrThrow(_cursor, "decade");
          final int _cursorIndexOfWordCount = CursorUtil.getColumnIndexOrThrow(_cursor, "word_count");
          final int _cursorIndexOfAudibleMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "audible_minutes");
          final int _cursorIndexOfStoryType = CursorUtil.getColumnIndexOrThrow(_cursor, "story_type");
          final int _cursorIndexOfKeywords = CursorUtil.getColumnIndexOrThrow(_cursor, "keywords");
          final int _cursorIndexOfGenres = CursorUtil.getColumnIndexOrThrow(_cursor, "genres");
          final int _cursorIndexOfIsCollectionParent = CursorUtil.getColumnIndexOrThrow(_cursor, "is_collection_parent");
          final int _cursorIndexOfCollection = CursorUtil.getColumnIndexOrThrow(_cursor, "collection");
          final int _cursorIndexOfCollectionId = CursorUtil.getColumnIndexOrThrow(_cursor, "collection_id");
          final int _cursorIndexOfChildIds = CursorUtil.getColumnIndexOrThrow(_cursor, "child_ids");
          final int _cursorIndexOfHasAdaptation = CursorUtil.getColumnIndexOrThrow(_cursor, "has_adaptation");
          final int _cursorIndexOfAdaptations = CursorUtil.getColumnIndexOrThrow(_cursor, "adaptations");
          final int _cursorIndexOfImdbUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "imdb_url");
          final int _cursorIndexOfCoverLocalPath = CursorUtil.getColumnIndexOrThrow(_cursor, "cover_local_path");
          final int _cursorIndexOfCoverCandidateUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "cover_candidate_url");
          final int _cursorIndexOfGoodreadsRating = CursorUtil.getColumnIndexOrThrow(_cursor, "goodreads_rating");
          final int _cursorIndexOfGoodreadsRatingsCount = CursorUtil.getColumnIndexOrThrow(_cursor, "goodreads_ratings_count");
          final int _cursorIndexOfIsOwned = CursorUtil.getColumnIndexOrThrow(_cursor, "is_owned");
          final int _cursorIndexOfIsRead = CursorUtil.getColumnIndexOrThrow(_cursor, "is_read");
          final int _cursorIndexOfIsReadingNow = CursorUtil.getColumnIndexOrThrow(_cursor, "is_reading_now");
          final int _cursorIndexOfIsOnReadingList = CursorUtil.getColumnIndexOrThrow(_cursor, "is_on_reading_list");
          final int _cursorIndexOfLastStatusChanged = CursorUtil.getColumnIndexOrThrow(_cursor, "last_status_changed");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final List<BookEntity> _result = new ArrayList<BookEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final BookEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpAuthor;
            _tmpAuthor = _cursor.getString(_cursorIndexOfAuthor);
            final boolean _tmpAsBachman;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfAsBachman);
            _tmpAsBachman = _tmp != 0;
            final Integer _tmpYear;
            if (_cursor.isNull(_cursorIndexOfYear)) {
              _tmpYear = null;
            } else {
              _tmpYear = _cursor.getInt(_cursorIndexOfYear);
            }
            final Integer _tmpDecade;
            if (_cursor.isNull(_cursorIndexOfDecade)) {
              _tmpDecade = null;
            } else {
              _tmpDecade = _cursor.getInt(_cursorIndexOfDecade);
            }
            final Integer _tmpWordCount;
            if (_cursor.isNull(_cursorIndexOfWordCount)) {
              _tmpWordCount = null;
            } else {
              _tmpWordCount = _cursor.getInt(_cursorIndexOfWordCount);
            }
            final Integer _tmpAudibleMinutes;
            if (_cursor.isNull(_cursorIndexOfAudibleMinutes)) {
              _tmpAudibleMinutes = null;
            } else {
              _tmpAudibleMinutes = _cursor.getInt(_cursorIndexOfAudibleMinutes);
            }
            final String _tmpStoryType;
            _tmpStoryType = _cursor.getString(_cursorIndexOfStoryType);
            final List<String> _tmpKeywords;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfKeywords);
            _tmpKeywords = __converters.fromStringList(_tmp_1);
            final List<String> _tmpGenres;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfGenres);
            _tmpGenres = __converters.fromStringList(_tmp_2);
            final boolean _tmpIsCollectionParent;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsCollectionParent);
            _tmpIsCollectionParent = _tmp_3 != 0;
            final String _tmpCollection;
            if (_cursor.isNull(_cursorIndexOfCollection)) {
              _tmpCollection = null;
            } else {
              _tmpCollection = _cursor.getString(_cursorIndexOfCollection);
            }
            final Integer _tmpCollectionId;
            if (_cursor.isNull(_cursorIndexOfCollectionId)) {
              _tmpCollectionId = null;
            } else {
              _tmpCollectionId = _cursor.getInt(_cursorIndexOfCollectionId);
            }
            final List<Integer> _tmpChildIds;
            final String _tmp_4;
            _tmp_4 = _cursor.getString(_cursorIndexOfChildIds);
            _tmpChildIds = __converters.fromIntList(_tmp_4);
            final boolean _tmpHasAdaptation;
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfHasAdaptation);
            _tmpHasAdaptation = _tmp_5 != 0;
            final List<Adaptation> _tmpAdaptations;
            final String _tmp_6;
            _tmp_6 = _cursor.getString(_cursorIndexOfAdaptations);
            _tmpAdaptations = __converters.fromAdaptationList(_tmp_6);
            final String _tmpImdbUrl;
            if (_cursor.isNull(_cursorIndexOfImdbUrl)) {
              _tmpImdbUrl = null;
            } else {
              _tmpImdbUrl = _cursor.getString(_cursorIndexOfImdbUrl);
            }
            final String _tmpCoverLocalPath;
            if (_cursor.isNull(_cursorIndexOfCoverLocalPath)) {
              _tmpCoverLocalPath = null;
            } else {
              _tmpCoverLocalPath = _cursor.getString(_cursorIndexOfCoverLocalPath);
            }
            final String _tmpCoverCandidateUrl;
            if (_cursor.isNull(_cursorIndexOfCoverCandidateUrl)) {
              _tmpCoverCandidateUrl = null;
            } else {
              _tmpCoverCandidateUrl = _cursor.getString(_cursorIndexOfCoverCandidateUrl);
            }
            final Float _tmpGoodreadsRating;
            if (_cursor.isNull(_cursorIndexOfGoodreadsRating)) {
              _tmpGoodreadsRating = null;
            } else {
              _tmpGoodreadsRating = _cursor.getFloat(_cursorIndexOfGoodreadsRating);
            }
            final Integer _tmpGoodreadsRatingsCount;
            if (_cursor.isNull(_cursorIndexOfGoodreadsRatingsCount)) {
              _tmpGoodreadsRatingsCount = null;
            } else {
              _tmpGoodreadsRatingsCount = _cursor.getInt(_cursorIndexOfGoodreadsRatingsCount);
            }
            final boolean _tmpIsOwned;
            final int _tmp_7;
            _tmp_7 = _cursor.getInt(_cursorIndexOfIsOwned);
            _tmpIsOwned = _tmp_7 != 0;
            final boolean _tmpIsRead;
            final int _tmp_8;
            _tmp_8 = _cursor.getInt(_cursorIndexOfIsRead);
            _tmpIsRead = _tmp_8 != 0;
            final boolean _tmpIsReadingNow;
            final int _tmp_9;
            _tmp_9 = _cursor.getInt(_cursorIndexOfIsReadingNow);
            _tmpIsReadingNow = _tmp_9 != 0;
            final boolean _tmpIsOnReadingList;
            final int _tmp_10;
            _tmp_10 = _cursor.getInt(_cursorIndexOfIsOnReadingList);
            _tmpIsOnReadingList = _tmp_10 != 0;
            final Long _tmpLastStatusChanged;
            if (_cursor.isNull(_cursorIndexOfLastStatusChanged)) {
              _tmpLastStatusChanged = null;
            } else {
              _tmpLastStatusChanged = _cursor.getLong(_cursorIndexOfLastStatusChanged);
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            _item = new BookEntity(_tmpId,_tmpTitle,_tmpAuthor,_tmpAsBachman,_tmpYear,_tmpDecade,_tmpWordCount,_tmpAudibleMinutes,_tmpStoryType,_tmpKeywords,_tmpGenres,_tmpIsCollectionParent,_tmpCollection,_tmpCollectionId,_tmpChildIds,_tmpHasAdaptation,_tmpAdaptations,_tmpImdbUrl,_tmpCoverLocalPath,_tmpCoverCandidateUrl,_tmpGoodreadsRating,_tmpGoodreadsRatingsCount,_tmpIsOwned,_tmpIsRead,_tmpIsReadingNow,_tmpIsOnReadingList,_tmpLastStatusChanged,_tmpNotes);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<BookEntity>> observeOtherAuthors() {
    final String _sql = "\n"
            + "        SELECT * FROM books\n"
            + "        WHERE author NOT IN ('Stephen King', 'Richard Bachman')\n"
            + "          AND (is_collection_parent = 1 OR collection_id IS NULL)\n"
            + "        ORDER BY year ASC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"books"}, new Callable<List<BookEntity>>() {
      @Override
      @NonNull
      public List<BookEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfAuthor = CursorUtil.getColumnIndexOrThrow(_cursor, "author");
          final int _cursorIndexOfAsBachman = CursorUtil.getColumnIndexOrThrow(_cursor, "as_bachman");
          final int _cursorIndexOfYear = CursorUtil.getColumnIndexOrThrow(_cursor, "year");
          final int _cursorIndexOfDecade = CursorUtil.getColumnIndexOrThrow(_cursor, "decade");
          final int _cursorIndexOfWordCount = CursorUtil.getColumnIndexOrThrow(_cursor, "word_count");
          final int _cursorIndexOfAudibleMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "audible_minutes");
          final int _cursorIndexOfStoryType = CursorUtil.getColumnIndexOrThrow(_cursor, "story_type");
          final int _cursorIndexOfKeywords = CursorUtil.getColumnIndexOrThrow(_cursor, "keywords");
          final int _cursorIndexOfGenres = CursorUtil.getColumnIndexOrThrow(_cursor, "genres");
          final int _cursorIndexOfIsCollectionParent = CursorUtil.getColumnIndexOrThrow(_cursor, "is_collection_parent");
          final int _cursorIndexOfCollection = CursorUtil.getColumnIndexOrThrow(_cursor, "collection");
          final int _cursorIndexOfCollectionId = CursorUtil.getColumnIndexOrThrow(_cursor, "collection_id");
          final int _cursorIndexOfChildIds = CursorUtil.getColumnIndexOrThrow(_cursor, "child_ids");
          final int _cursorIndexOfHasAdaptation = CursorUtil.getColumnIndexOrThrow(_cursor, "has_adaptation");
          final int _cursorIndexOfAdaptations = CursorUtil.getColumnIndexOrThrow(_cursor, "adaptations");
          final int _cursorIndexOfImdbUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "imdb_url");
          final int _cursorIndexOfCoverLocalPath = CursorUtil.getColumnIndexOrThrow(_cursor, "cover_local_path");
          final int _cursorIndexOfCoverCandidateUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "cover_candidate_url");
          final int _cursorIndexOfGoodreadsRating = CursorUtil.getColumnIndexOrThrow(_cursor, "goodreads_rating");
          final int _cursorIndexOfGoodreadsRatingsCount = CursorUtil.getColumnIndexOrThrow(_cursor, "goodreads_ratings_count");
          final int _cursorIndexOfIsOwned = CursorUtil.getColumnIndexOrThrow(_cursor, "is_owned");
          final int _cursorIndexOfIsRead = CursorUtil.getColumnIndexOrThrow(_cursor, "is_read");
          final int _cursorIndexOfIsReadingNow = CursorUtil.getColumnIndexOrThrow(_cursor, "is_reading_now");
          final int _cursorIndexOfIsOnReadingList = CursorUtil.getColumnIndexOrThrow(_cursor, "is_on_reading_list");
          final int _cursorIndexOfLastStatusChanged = CursorUtil.getColumnIndexOrThrow(_cursor, "last_status_changed");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final List<BookEntity> _result = new ArrayList<BookEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final BookEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpAuthor;
            _tmpAuthor = _cursor.getString(_cursorIndexOfAuthor);
            final boolean _tmpAsBachman;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfAsBachman);
            _tmpAsBachman = _tmp != 0;
            final Integer _tmpYear;
            if (_cursor.isNull(_cursorIndexOfYear)) {
              _tmpYear = null;
            } else {
              _tmpYear = _cursor.getInt(_cursorIndexOfYear);
            }
            final Integer _tmpDecade;
            if (_cursor.isNull(_cursorIndexOfDecade)) {
              _tmpDecade = null;
            } else {
              _tmpDecade = _cursor.getInt(_cursorIndexOfDecade);
            }
            final Integer _tmpWordCount;
            if (_cursor.isNull(_cursorIndexOfWordCount)) {
              _tmpWordCount = null;
            } else {
              _tmpWordCount = _cursor.getInt(_cursorIndexOfWordCount);
            }
            final Integer _tmpAudibleMinutes;
            if (_cursor.isNull(_cursorIndexOfAudibleMinutes)) {
              _tmpAudibleMinutes = null;
            } else {
              _tmpAudibleMinutes = _cursor.getInt(_cursorIndexOfAudibleMinutes);
            }
            final String _tmpStoryType;
            _tmpStoryType = _cursor.getString(_cursorIndexOfStoryType);
            final List<String> _tmpKeywords;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfKeywords);
            _tmpKeywords = __converters.fromStringList(_tmp_1);
            final List<String> _tmpGenres;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfGenres);
            _tmpGenres = __converters.fromStringList(_tmp_2);
            final boolean _tmpIsCollectionParent;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsCollectionParent);
            _tmpIsCollectionParent = _tmp_3 != 0;
            final String _tmpCollection;
            if (_cursor.isNull(_cursorIndexOfCollection)) {
              _tmpCollection = null;
            } else {
              _tmpCollection = _cursor.getString(_cursorIndexOfCollection);
            }
            final Integer _tmpCollectionId;
            if (_cursor.isNull(_cursorIndexOfCollectionId)) {
              _tmpCollectionId = null;
            } else {
              _tmpCollectionId = _cursor.getInt(_cursorIndexOfCollectionId);
            }
            final List<Integer> _tmpChildIds;
            final String _tmp_4;
            _tmp_4 = _cursor.getString(_cursorIndexOfChildIds);
            _tmpChildIds = __converters.fromIntList(_tmp_4);
            final boolean _tmpHasAdaptation;
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfHasAdaptation);
            _tmpHasAdaptation = _tmp_5 != 0;
            final List<Adaptation> _tmpAdaptations;
            final String _tmp_6;
            _tmp_6 = _cursor.getString(_cursorIndexOfAdaptations);
            _tmpAdaptations = __converters.fromAdaptationList(_tmp_6);
            final String _tmpImdbUrl;
            if (_cursor.isNull(_cursorIndexOfImdbUrl)) {
              _tmpImdbUrl = null;
            } else {
              _tmpImdbUrl = _cursor.getString(_cursorIndexOfImdbUrl);
            }
            final String _tmpCoverLocalPath;
            if (_cursor.isNull(_cursorIndexOfCoverLocalPath)) {
              _tmpCoverLocalPath = null;
            } else {
              _tmpCoverLocalPath = _cursor.getString(_cursorIndexOfCoverLocalPath);
            }
            final String _tmpCoverCandidateUrl;
            if (_cursor.isNull(_cursorIndexOfCoverCandidateUrl)) {
              _tmpCoverCandidateUrl = null;
            } else {
              _tmpCoverCandidateUrl = _cursor.getString(_cursorIndexOfCoverCandidateUrl);
            }
            final Float _tmpGoodreadsRating;
            if (_cursor.isNull(_cursorIndexOfGoodreadsRating)) {
              _tmpGoodreadsRating = null;
            } else {
              _tmpGoodreadsRating = _cursor.getFloat(_cursorIndexOfGoodreadsRating);
            }
            final Integer _tmpGoodreadsRatingsCount;
            if (_cursor.isNull(_cursorIndexOfGoodreadsRatingsCount)) {
              _tmpGoodreadsRatingsCount = null;
            } else {
              _tmpGoodreadsRatingsCount = _cursor.getInt(_cursorIndexOfGoodreadsRatingsCount);
            }
            final boolean _tmpIsOwned;
            final int _tmp_7;
            _tmp_7 = _cursor.getInt(_cursorIndexOfIsOwned);
            _tmpIsOwned = _tmp_7 != 0;
            final boolean _tmpIsRead;
            final int _tmp_8;
            _tmp_8 = _cursor.getInt(_cursorIndexOfIsRead);
            _tmpIsRead = _tmp_8 != 0;
            final boolean _tmpIsReadingNow;
            final int _tmp_9;
            _tmp_9 = _cursor.getInt(_cursorIndexOfIsReadingNow);
            _tmpIsReadingNow = _tmp_9 != 0;
            final boolean _tmpIsOnReadingList;
            final int _tmp_10;
            _tmp_10 = _cursor.getInt(_cursorIndexOfIsOnReadingList);
            _tmpIsOnReadingList = _tmp_10 != 0;
            final Long _tmpLastStatusChanged;
            if (_cursor.isNull(_cursorIndexOfLastStatusChanged)) {
              _tmpLastStatusChanged = null;
            } else {
              _tmpLastStatusChanged = _cursor.getLong(_cursorIndexOfLastStatusChanged);
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            _item = new BookEntity(_tmpId,_tmpTitle,_tmpAuthor,_tmpAsBachman,_tmpYear,_tmpDecade,_tmpWordCount,_tmpAudibleMinutes,_tmpStoryType,_tmpKeywords,_tmpGenres,_tmpIsCollectionParent,_tmpCollection,_tmpCollectionId,_tmpChildIds,_tmpHasAdaptation,_tmpAdaptations,_tmpImdbUrl,_tmpCoverLocalPath,_tmpCoverCandidateUrl,_tmpGoodreadsRating,_tmpGoodreadsRatingsCount,_tmpIsOwned,_tmpIsRead,_tmpIsReadingNow,_tmpIsOnReadingList,_tmpLastStatusChanged,_tmpNotes);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<BookEntity>> observeOwned() {
    final String _sql = "SELECT * FROM books WHERE is_owned = 1 ORDER BY author ASC, title ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"books"}, new Callable<List<BookEntity>>() {
      @Override
      @NonNull
      public List<BookEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfAuthor = CursorUtil.getColumnIndexOrThrow(_cursor, "author");
          final int _cursorIndexOfAsBachman = CursorUtil.getColumnIndexOrThrow(_cursor, "as_bachman");
          final int _cursorIndexOfYear = CursorUtil.getColumnIndexOrThrow(_cursor, "year");
          final int _cursorIndexOfDecade = CursorUtil.getColumnIndexOrThrow(_cursor, "decade");
          final int _cursorIndexOfWordCount = CursorUtil.getColumnIndexOrThrow(_cursor, "word_count");
          final int _cursorIndexOfAudibleMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "audible_minutes");
          final int _cursorIndexOfStoryType = CursorUtil.getColumnIndexOrThrow(_cursor, "story_type");
          final int _cursorIndexOfKeywords = CursorUtil.getColumnIndexOrThrow(_cursor, "keywords");
          final int _cursorIndexOfGenres = CursorUtil.getColumnIndexOrThrow(_cursor, "genres");
          final int _cursorIndexOfIsCollectionParent = CursorUtil.getColumnIndexOrThrow(_cursor, "is_collection_parent");
          final int _cursorIndexOfCollection = CursorUtil.getColumnIndexOrThrow(_cursor, "collection");
          final int _cursorIndexOfCollectionId = CursorUtil.getColumnIndexOrThrow(_cursor, "collection_id");
          final int _cursorIndexOfChildIds = CursorUtil.getColumnIndexOrThrow(_cursor, "child_ids");
          final int _cursorIndexOfHasAdaptation = CursorUtil.getColumnIndexOrThrow(_cursor, "has_adaptation");
          final int _cursorIndexOfAdaptations = CursorUtil.getColumnIndexOrThrow(_cursor, "adaptations");
          final int _cursorIndexOfImdbUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "imdb_url");
          final int _cursorIndexOfCoverLocalPath = CursorUtil.getColumnIndexOrThrow(_cursor, "cover_local_path");
          final int _cursorIndexOfCoverCandidateUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "cover_candidate_url");
          final int _cursorIndexOfGoodreadsRating = CursorUtil.getColumnIndexOrThrow(_cursor, "goodreads_rating");
          final int _cursorIndexOfGoodreadsRatingsCount = CursorUtil.getColumnIndexOrThrow(_cursor, "goodreads_ratings_count");
          final int _cursorIndexOfIsOwned = CursorUtil.getColumnIndexOrThrow(_cursor, "is_owned");
          final int _cursorIndexOfIsRead = CursorUtil.getColumnIndexOrThrow(_cursor, "is_read");
          final int _cursorIndexOfIsReadingNow = CursorUtil.getColumnIndexOrThrow(_cursor, "is_reading_now");
          final int _cursorIndexOfIsOnReadingList = CursorUtil.getColumnIndexOrThrow(_cursor, "is_on_reading_list");
          final int _cursorIndexOfLastStatusChanged = CursorUtil.getColumnIndexOrThrow(_cursor, "last_status_changed");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final List<BookEntity> _result = new ArrayList<BookEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final BookEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpAuthor;
            _tmpAuthor = _cursor.getString(_cursorIndexOfAuthor);
            final boolean _tmpAsBachman;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfAsBachman);
            _tmpAsBachman = _tmp != 0;
            final Integer _tmpYear;
            if (_cursor.isNull(_cursorIndexOfYear)) {
              _tmpYear = null;
            } else {
              _tmpYear = _cursor.getInt(_cursorIndexOfYear);
            }
            final Integer _tmpDecade;
            if (_cursor.isNull(_cursorIndexOfDecade)) {
              _tmpDecade = null;
            } else {
              _tmpDecade = _cursor.getInt(_cursorIndexOfDecade);
            }
            final Integer _tmpWordCount;
            if (_cursor.isNull(_cursorIndexOfWordCount)) {
              _tmpWordCount = null;
            } else {
              _tmpWordCount = _cursor.getInt(_cursorIndexOfWordCount);
            }
            final Integer _tmpAudibleMinutes;
            if (_cursor.isNull(_cursorIndexOfAudibleMinutes)) {
              _tmpAudibleMinutes = null;
            } else {
              _tmpAudibleMinutes = _cursor.getInt(_cursorIndexOfAudibleMinutes);
            }
            final String _tmpStoryType;
            _tmpStoryType = _cursor.getString(_cursorIndexOfStoryType);
            final List<String> _tmpKeywords;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfKeywords);
            _tmpKeywords = __converters.fromStringList(_tmp_1);
            final List<String> _tmpGenres;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfGenres);
            _tmpGenres = __converters.fromStringList(_tmp_2);
            final boolean _tmpIsCollectionParent;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsCollectionParent);
            _tmpIsCollectionParent = _tmp_3 != 0;
            final String _tmpCollection;
            if (_cursor.isNull(_cursorIndexOfCollection)) {
              _tmpCollection = null;
            } else {
              _tmpCollection = _cursor.getString(_cursorIndexOfCollection);
            }
            final Integer _tmpCollectionId;
            if (_cursor.isNull(_cursorIndexOfCollectionId)) {
              _tmpCollectionId = null;
            } else {
              _tmpCollectionId = _cursor.getInt(_cursorIndexOfCollectionId);
            }
            final List<Integer> _tmpChildIds;
            final String _tmp_4;
            _tmp_4 = _cursor.getString(_cursorIndexOfChildIds);
            _tmpChildIds = __converters.fromIntList(_tmp_4);
            final boolean _tmpHasAdaptation;
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfHasAdaptation);
            _tmpHasAdaptation = _tmp_5 != 0;
            final List<Adaptation> _tmpAdaptations;
            final String _tmp_6;
            _tmp_6 = _cursor.getString(_cursorIndexOfAdaptations);
            _tmpAdaptations = __converters.fromAdaptationList(_tmp_6);
            final String _tmpImdbUrl;
            if (_cursor.isNull(_cursorIndexOfImdbUrl)) {
              _tmpImdbUrl = null;
            } else {
              _tmpImdbUrl = _cursor.getString(_cursorIndexOfImdbUrl);
            }
            final String _tmpCoverLocalPath;
            if (_cursor.isNull(_cursorIndexOfCoverLocalPath)) {
              _tmpCoverLocalPath = null;
            } else {
              _tmpCoverLocalPath = _cursor.getString(_cursorIndexOfCoverLocalPath);
            }
            final String _tmpCoverCandidateUrl;
            if (_cursor.isNull(_cursorIndexOfCoverCandidateUrl)) {
              _tmpCoverCandidateUrl = null;
            } else {
              _tmpCoverCandidateUrl = _cursor.getString(_cursorIndexOfCoverCandidateUrl);
            }
            final Float _tmpGoodreadsRating;
            if (_cursor.isNull(_cursorIndexOfGoodreadsRating)) {
              _tmpGoodreadsRating = null;
            } else {
              _tmpGoodreadsRating = _cursor.getFloat(_cursorIndexOfGoodreadsRating);
            }
            final Integer _tmpGoodreadsRatingsCount;
            if (_cursor.isNull(_cursorIndexOfGoodreadsRatingsCount)) {
              _tmpGoodreadsRatingsCount = null;
            } else {
              _tmpGoodreadsRatingsCount = _cursor.getInt(_cursorIndexOfGoodreadsRatingsCount);
            }
            final boolean _tmpIsOwned;
            final int _tmp_7;
            _tmp_7 = _cursor.getInt(_cursorIndexOfIsOwned);
            _tmpIsOwned = _tmp_7 != 0;
            final boolean _tmpIsRead;
            final int _tmp_8;
            _tmp_8 = _cursor.getInt(_cursorIndexOfIsRead);
            _tmpIsRead = _tmp_8 != 0;
            final boolean _tmpIsReadingNow;
            final int _tmp_9;
            _tmp_9 = _cursor.getInt(_cursorIndexOfIsReadingNow);
            _tmpIsReadingNow = _tmp_9 != 0;
            final boolean _tmpIsOnReadingList;
            final int _tmp_10;
            _tmp_10 = _cursor.getInt(_cursorIndexOfIsOnReadingList);
            _tmpIsOnReadingList = _tmp_10 != 0;
            final Long _tmpLastStatusChanged;
            if (_cursor.isNull(_cursorIndexOfLastStatusChanged)) {
              _tmpLastStatusChanged = null;
            } else {
              _tmpLastStatusChanged = _cursor.getLong(_cursorIndexOfLastStatusChanged);
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            _item = new BookEntity(_tmpId,_tmpTitle,_tmpAuthor,_tmpAsBachman,_tmpYear,_tmpDecade,_tmpWordCount,_tmpAudibleMinutes,_tmpStoryType,_tmpKeywords,_tmpGenres,_tmpIsCollectionParent,_tmpCollection,_tmpCollectionId,_tmpChildIds,_tmpHasAdaptation,_tmpAdaptations,_tmpImdbUrl,_tmpCoverLocalPath,_tmpCoverCandidateUrl,_tmpGoodreadsRating,_tmpGoodreadsRatingsCount,_tmpIsOwned,_tmpIsRead,_tmpIsReadingNow,_tmpIsOnReadingList,_tmpLastStatusChanged,_tmpNotes);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<BookEntity>> observeByAuthor(final String author) {
    final String _sql = "\n"
            + "        SELECT * FROM books\n"
            + "        WHERE author = ?\n"
            + "          AND (is_collection_parent = 1 OR collection_id IS NULL)\n"
            + "        ORDER BY year ASC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, author);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"books"}, new Callable<List<BookEntity>>() {
      @Override
      @NonNull
      public List<BookEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfAuthor = CursorUtil.getColumnIndexOrThrow(_cursor, "author");
          final int _cursorIndexOfAsBachman = CursorUtil.getColumnIndexOrThrow(_cursor, "as_bachman");
          final int _cursorIndexOfYear = CursorUtil.getColumnIndexOrThrow(_cursor, "year");
          final int _cursorIndexOfDecade = CursorUtil.getColumnIndexOrThrow(_cursor, "decade");
          final int _cursorIndexOfWordCount = CursorUtil.getColumnIndexOrThrow(_cursor, "word_count");
          final int _cursorIndexOfAudibleMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "audible_minutes");
          final int _cursorIndexOfStoryType = CursorUtil.getColumnIndexOrThrow(_cursor, "story_type");
          final int _cursorIndexOfKeywords = CursorUtil.getColumnIndexOrThrow(_cursor, "keywords");
          final int _cursorIndexOfGenres = CursorUtil.getColumnIndexOrThrow(_cursor, "genres");
          final int _cursorIndexOfIsCollectionParent = CursorUtil.getColumnIndexOrThrow(_cursor, "is_collection_parent");
          final int _cursorIndexOfCollection = CursorUtil.getColumnIndexOrThrow(_cursor, "collection");
          final int _cursorIndexOfCollectionId = CursorUtil.getColumnIndexOrThrow(_cursor, "collection_id");
          final int _cursorIndexOfChildIds = CursorUtil.getColumnIndexOrThrow(_cursor, "child_ids");
          final int _cursorIndexOfHasAdaptation = CursorUtil.getColumnIndexOrThrow(_cursor, "has_adaptation");
          final int _cursorIndexOfAdaptations = CursorUtil.getColumnIndexOrThrow(_cursor, "adaptations");
          final int _cursorIndexOfImdbUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "imdb_url");
          final int _cursorIndexOfCoverLocalPath = CursorUtil.getColumnIndexOrThrow(_cursor, "cover_local_path");
          final int _cursorIndexOfCoverCandidateUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "cover_candidate_url");
          final int _cursorIndexOfGoodreadsRating = CursorUtil.getColumnIndexOrThrow(_cursor, "goodreads_rating");
          final int _cursorIndexOfGoodreadsRatingsCount = CursorUtil.getColumnIndexOrThrow(_cursor, "goodreads_ratings_count");
          final int _cursorIndexOfIsOwned = CursorUtil.getColumnIndexOrThrow(_cursor, "is_owned");
          final int _cursorIndexOfIsRead = CursorUtil.getColumnIndexOrThrow(_cursor, "is_read");
          final int _cursorIndexOfIsReadingNow = CursorUtil.getColumnIndexOrThrow(_cursor, "is_reading_now");
          final int _cursorIndexOfIsOnReadingList = CursorUtil.getColumnIndexOrThrow(_cursor, "is_on_reading_list");
          final int _cursorIndexOfLastStatusChanged = CursorUtil.getColumnIndexOrThrow(_cursor, "last_status_changed");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final List<BookEntity> _result = new ArrayList<BookEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final BookEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpAuthor;
            _tmpAuthor = _cursor.getString(_cursorIndexOfAuthor);
            final boolean _tmpAsBachman;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfAsBachman);
            _tmpAsBachman = _tmp != 0;
            final Integer _tmpYear;
            if (_cursor.isNull(_cursorIndexOfYear)) {
              _tmpYear = null;
            } else {
              _tmpYear = _cursor.getInt(_cursorIndexOfYear);
            }
            final Integer _tmpDecade;
            if (_cursor.isNull(_cursorIndexOfDecade)) {
              _tmpDecade = null;
            } else {
              _tmpDecade = _cursor.getInt(_cursorIndexOfDecade);
            }
            final Integer _tmpWordCount;
            if (_cursor.isNull(_cursorIndexOfWordCount)) {
              _tmpWordCount = null;
            } else {
              _tmpWordCount = _cursor.getInt(_cursorIndexOfWordCount);
            }
            final Integer _tmpAudibleMinutes;
            if (_cursor.isNull(_cursorIndexOfAudibleMinutes)) {
              _tmpAudibleMinutes = null;
            } else {
              _tmpAudibleMinutes = _cursor.getInt(_cursorIndexOfAudibleMinutes);
            }
            final String _tmpStoryType;
            _tmpStoryType = _cursor.getString(_cursorIndexOfStoryType);
            final List<String> _tmpKeywords;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfKeywords);
            _tmpKeywords = __converters.fromStringList(_tmp_1);
            final List<String> _tmpGenres;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfGenres);
            _tmpGenres = __converters.fromStringList(_tmp_2);
            final boolean _tmpIsCollectionParent;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsCollectionParent);
            _tmpIsCollectionParent = _tmp_3 != 0;
            final String _tmpCollection;
            if (_cursor.isNull(_cursorIndexOfCollection)) {
              _tmpCollection = null;
            } else {
              _tmpCollection = _cursor.getString(_cursorIndexOfCollection);
            }
            final Integer _tmpCollectionId;
            if (_cursor.isNull(_cursorIndexOfCollectionId)) {
              _tmpCollectionId = null;
            } else {
              _tmpCollectionId = _cursor.getInt(_cursorIndexOfCollectionId);
            }
            final List<Integer> _tmpChildIds;
            final String _tmp_4;
            _tmp_4 = _cursor.getString(_cursorIndexOfChildIds);
            _tmpChildIds = __converters.fromIntList(_tmp_4);
            final boolean _tmpHasAdaptation;
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfHasAdaptation);
            _tmpHasAdaptation = _tmp_5 != 0;
            final List<Adaptation> _tmpAdaptations;
            final String _tmp_6;
            _tmp_6 = _cursor.getString(_cursorIndexOfAdaptations);
            _tmpAdaptations = __converters.fromAdaptationList(_tmp_6);
            final String _tmpImdbUrl;
            if (_cursor.isNull(_cursorIndexOfImdbUrl)) {
              _tmpImdbUrl = null;
            } else {
              _tmpImdbUrl = _cursor.getString(_cursorIndexOfImdbUrl);
            }
            final String _tmpCoverLocalPath;
            if (_cursor.isNull(_cursorIndexOfCoverLocalPath)) {
              _tmpCoverLocalPath = null;
            } else {
              _tmpCoverLocalPath = _cursor.getString(_cursorIndexOfCoverLocalPath);
            }
            final String _tmpCoverCandidateUrl;
            if (_cursor.isNull(_cursorIndexOfCoverCandidateUrl)) {
              _tmpCoverCandidateUrl = null;
            } else {
              _tmpCoverCandidateUrl = _cursor.getString(_cursorIndexOfCoverCandidateUrl);
            }
            final Float _tmpGoodreadsRating;
            if (_cursor.isNull(_cursorIndexOfGoodreadsRating)) {
              _tmpGoodreadsRating = null;
            } else {
              _tmpGoodreadsRating = _cursor.getFloat(_cursorIndexOfGoodreadsRating);
            }
            final Integer _tmpGoodreadsRatingsCount;
            if (_cursor.isNull(_cursorIndexOfGoodreadsRatingsCount)) {
              _tmpGoodreadsRatingsCount = null;
            } else {
              _tmpGoodreadsRatingsCount = _cursor.getInt(_cursorIndexOfGoodreadsRatingsCount);
            }
            final boolean _tmpIsOwned;
            final int _tmp_7;
            _tmp_7 = _cursor.getInt(_cursorIndexOfIsOwned);
            _tmpIsOwned = _tmp_7 != 0;
            final boolean _tmpIsRead;
            final int _tmp_8;
            _tmp_8 = _cursor.getInt(_cursorIndexOfIsRead);
            _tmpIsRead = _tmp_8 != 0;
            final boolean _tmpIsReadingNow;
            final int _tmp_9;
            _tmp_9 = _cursor.getInt(_cursorIndexOfIsReadingNow);
            _tmpIsReadingNow = _tmp_9 != 0;
            final boolean _tmpIsOnReadingList;
            final int _tmp_10;
            _tmp_10 = _cursor.getInt(_cursorIndexOfIsOnReadingList);
            _tmpIsOnReadingList = _tmp_10 != 0;
            final Long _tmpLastStatusChanged;
            if (_cursor.isNull(_cursorIndexOfLastStatusChanged)) {
              _tmpLastStatusChanged = null;
            } else {
              _tmpLastStatusChanged = _cursor.getLong(_cursorIndexOfLastStatusChanged);
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            _item = new BookEntity(_tmpId,_tmpTitle,_tmpAuthor,_tmpAsBachman,_tmpYear,_tmpDecade,_tmpWordCount,_tmpAudibleMinutes,_tmpStoryType,_tmpKeywords,_tmpGenres,_tmpIsCollectionParent,_tmpCollection,_tmpCollectionId,_tmpChildIds,_tmpHasAdaptation,_tmpAdaptations,_tmpImdbUrl,_tmpCoverLocalPath,_tmpCoverCandidateUrl,_tmpGoodreadsRating,_tmpGoodreadsRatingsCount,_tmpIsOwned,_tmpIsRead,_tmpIsReadingNow,_tmpIsOnReadingList,_tmpLastStatusChanged,_tmpNotes);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<BookEntity>> observeReadingNow() {
    final String _sql = "SELECT * FROM books WHERE is_reading_now = 1 ORDER BY last_status_changed DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"books"}, new Callable<List<BookEntity>>() {
      @Override
      @NonNull
      public List<BookEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfAuthor = CursorUtil.getColumnIndexOrThrow(_cursor, "author");
          final int _cursorIndexOfAsBachman = CursorUtil.getColumnIndexOrThrow(_cursor, "as_bachman");
          final int _cursorIndexOfYear = CursorUtil.getColumnIndexOrThrow(_cursor, "year");
          final int _cursorIndexOfDecade = CursorUtil.getColumnIndexOrThrow(_cursor, "decade");
          final int _cursorIndexOfWordCount = CursorUtil.getColumnIndexOrThrow(_cursor, "word_count");
          final int _cursorIndexOfAudibleMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "audible_minutes");
          final int _cursorIndexOfStoryType = CursorUtil.getColumnIndexOrThrow(_cursor, "story_type");
          final int _cursorIndexOfKeywords = CursorUtil.getColumnIndexOrThrow(_cursor, "keywords");
          final int _cursorIndexOfGenres = CursorUtil.getColumnIndexOrThrow(_cursor, "genres");
          final int _cursorIndexOfIsCollectionParent = CursorUtil.getColumnIndexOrThrow(_cursor, "is_collection_parent");
          final int _cursorIndexOfCollection = CursorUtil.getColumnIndexOrThrow(_cursor, "collection");
          final int _cursorIndexOfCollectionId = CursorUtil.getColumnIndexOrThrow(_cursor, "collection_id");
          final int _cursorIndexOfChildIds = CursorUtil.getColumnIndexOrThrow(_cursor, "child_ids");
          final int _cursorIndexOfHasAdaptation = CursorUtil.getColumnIndexOrThrow(_cursor, "has_adaptation");
          final int _cursorIndexOfAdaptations = CursorUtil.getColumnIndexOrThrow(_cursor, "adaptations");
          final int _cursorIndexOfImdbUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "imdb_url");
          final int _cursorIndexOfCoverLocalPath = CursorUtil.getColumnIndexOrThrow(_cursor, "cover_local_path");
          final int _cursorIndexOfCoverCandidateUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "cover_candidate_url");
          final int _cursorIndexOfGoodreadsRating = CursorUtil.getColumnIndexOrThrow(_cursor, "goodreads_rating");
          final int _cursorIndexOfGoodreadsRatingsCount = CursorUtil.getColumnIndexOrThrow(_cursor, "goodreads_ratings_count");
          final int _cursorIndexOfIsOwned = CursorUtil.getColumnIndexOrThrow(_cursor, "is_owned");
          final int _cursorIndexOfIsRead = CursorUtil.getColumnIndexOrThrow(_cursor, "is_read");
          final int _cursorIndexOfIsReadingNow = CursorUtil.getColumnIndexOrThrow(_cursor, "is_reading_now");
          final int _cursorIndexOfIsOnReadingList = CursorUtil.getColumnIndexOrThrow(_cursor, "is_on_reading_list");
          final int _cursorIndexOfLastStatusChanged = CursorUtil.getColumnIndexOrThrow(_cursor, "last_status_changed");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final List<BookEntity> _result = new ArrayList<BookEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final BookEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpAuthor;
            _tmpAuthor = _cursor.getString(_cursorIndexOfAuthor);
            final boolean _tmpAsBachman;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfAsBachman);
            _tmpAsBachman = _tmp != 0;
            final Integer _tmpYear;
            if (_cursor.isNull(_cursorIndexOfYear)) {
              _tmpYear = null;
            } else {
              _tmpYear = _cursor.getInt(_cursorIndexOfYear);
            }
            final Integer _tmpDecade;
            if (_cursor.isNull(_cursorIndexOfDecade)) {
              _tmpDecade = null;
            } else {
              _tmpDecade = _cursor.getInt(_cursorIndexOfDecade);
            }
            final Integer _tmpWordCount;
            if (_cursor.isNull(_cursorIndexOfWordCount)) {
              _tmpWordCount = null;
            } else {
              _tmpWordCount = _cursor.getInt(_cursorIndexOfWordCount);
            }
            final Integer _tmpAudibleMinutes;
            if (_cursor.isNull(_cursorIndexOfAudibleMinutes)) {
              _tmpAudibleMinutes = null;
            } else {
              _tmpAudibleMinutes = _cursor.getInt(_cursorIndexOfAudibleMinutes);
            }
            final String _tmpStoryType;
            _tmpStoryType = _cursor.getString(_cursorIndexOfStoryType);
            final List<String> _tmpKeywords;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfKeywords);
            _tmpKeywords = __converters.fromStringList(_tmp_1);
            final List<String> _tmpGenres;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfGenres);
            _tmpGenres = __converters.fromStringList(_tmp_2);
            final boolean _tmpIsCollectionParent;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsCollectionParent);
            _tmpIsCollectionParent = _tmp_3 != 0;
            final String _tmpCollection;
            if (_cursor.isNull(_cursorIndexOfCollection)) {
              _tmpCollection = null;
            } else {
              _tmpCollection = _cursor.getString(_cursorIndexOfCollection);
            }
            final Integer _tmpCollectionId;
            if (_cursor.isNull(_cursorIndexOfCollectionId)) {
              _tmpCollectionId = null;
            } else {
              _tmpCollectionId = _cursor.getInt(_cursorIndexOfCollectionId);
            }
            final List<Integer> _tmpChildIds;
            final String _tmp_4;
            _tmp_4 = _cursor.getString(_cursorIndexOfChildIds);
            _tmpChildIds = __converters.fromIntList(_tmp_4);
            final boolean _tmpHasAdaptation;
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfHasAdaptation);
            _tmpHasAdaptation = _tmp_5 != 0;
            final List<Adaptation> _tmpAdaptations;
            final String _tmp_6;
            _tmp_6 = _cursor.getString(_cursorIndexOfAdaptations);
            _tmpAdaptations = __converters.fromAdaptationList(_tmp_6);
            final String _tmpImdbUrl;
            if (_cursor.isNull(_cursorIndexOfImdbUrl)) {
              _tmpImdbUrl = null;
            } else {
              _tmpImdbUrl = _cursor.getString(_cursorIndexOfImdbUrl);
            }
            final String _tmpCoverLocalPath;
            if (_cursor.isNull(_cursorIndexOfCoverLocalPath)) {
              _tmpCoverLocalPath = null;
            } else {
              _tmpCoverLocalPath = _cursor.getString(_cursorIndexOfCoverLocalPath);
            }
            final String _tmpCoverCandidateUrl;
            if (_cursor.isNull(_cursorIndexOfCoverCandidateUrl)) {
              _tmpCoverCandidateUrl = null;
            } else {
              _tmpCoverCandidateUrl = _cursor.getString(_cursorIndexOfCoverCandidateUrl);
            }
            final Float _tmpGoodreadsRating;
            if (_cursor.isNull(_cursorIndexOfGoodreadsRating)) {
              _tmpGoodreadsRating = null;
            } else {
              _tmpGoodreadsRating = _cursor.getFloat(_cursorIndexOfGoodreadsRating);
            }
            final Integer _tmpGoodreadsRatingsCount;
            if (_cursor.isNull(_cursorIndexOfGoodreadsRatingsCount)) {
              _tmpGoodreadsRatingsCount = null;
            } else {
              _tmpGoodreadsRatingsCount = _cursor.getInt(_cursorIndexOfGoodreadsRatingsCount);
            }
            final boolean _tmpIsOwned;
            final int _tmp_7;
            _tmp_7 = _cursor.getInt(_cursorIndexOfIsOwned);
            _tmpIsOwned = _tmp_7 != 0;
            final boolean _tmpIsRead;
            final int _tmp_8;
            _tmp_8 = _cursor.getInt(_cursorIndexOfIsRead);
            _tmpIsRead = _tmp_8 != 0;
            final boolean _tmpIsReadingNow;
            final int _tmp_9;
            _tmp_9 = _cursor.getInt(_cursorIndexOfIsReadingNow);
            _tmpIsReadingNow = _tmp_9 != 0;
            final boolean _tmpIsOnReadingList;
            final int _tmp_10;
            _tmp_10 = _cursor.getInt(_cursorIndexOfIsOnReadingList);
            _tmpIsOnReadingList = _tmp_10 != 0;
            final Long _tmpLastStatusChanged;
            if (_cursor.isNull(_cursorIndexOfLastStatusChanged)) {
              _tmpLastStatusChanged = null;
            } else {
              _tmpLastStatusChanged = _cursor.getLong(_cursorIndexOfLastStatusChanged);
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            _item = new BookEntity(_tmpId,_tmpTitle,_tmpAuthor,_tmpAsBachman,_tmpYear,_tmpDecade,_tmpWordCount,_tmpAudibleMinutes,_tmpStoryType,_tmpKeywords,_tmpGenres,_tmpIsCollectionParent,_tmpCollection,_tmpCollectionId,_tmpChildIds,_tmpHasAdaptation,_tmpAdaptations,_tmpImdbUrl,_tmpCoverLocalPath,_tmpCoverCandidateUrl,_tmpGoodreadsRating,_tmpGoodreadsRatingsCount,_tmpIsOwned,_tmpIsRead,_tmpIsReadingNow,_tmpIsOnReadingList,_tmpLastStatusChanged,_tmpNotes);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<BookEntity>> observeRecentlyRead() {
    final String _sql = "SELECT * FROM books WHERE is_read = 1 ORDER BY last_status_changed DESC LIMIT 10";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"books"}, new Callable<List<BookEntity>>() {
      @Override
      @NonNull
      public List<BookEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfAuthor = CursorUtil.getColumnIndexOrThrow(_cursor, "author");
          final int _cursorIndexOfAsBachman = CursorUtil.getColumnIndexOrThrow(_cursor, "as_bachman");
          final int _cursorIndexOfYear = CursorUtil.getColumnIndexOrThrow(_cursor, "year");
          final int _cursorIndexOfDecade = CursorUtil.getColumnIndexOrThrow(_cursor, "decade");
          final int _cursorIndexOfWordCount = CursorUtil.getColumnIndexOrThrow(_cursor, "word_count");
          final int _cursorIndexOfAudibleMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "audible_minutes");
          final int _cursorIndexOfStoryType = CursorUtil.getColumnIndexOrThrow(_cursor, "story_type");
          final int _cursorIndexOfKeywords = CursorUtil.getColumnIndexOrThrow(_cursor, "keywords");
          final int _cursorIndexOfGenres = CursorUtil.getColumnIndexOrThrow(_cursor, "genres");
          final int _cursorIndexOfIsCollectionParent = CursorUtil.getColumnIndexOrThrow(_cursor, "is_collection_parent");
          final int _cursorIndexOfCollection = CursorUtil.getColumnIndexOrThrow(_cursor, "collection");
          final int _cursorIndexOfCollectionId = CursorUtil.getColumnIndexOrThrow(_cursor, "collection_id");
          final int _cursorIndexOfChildIds = CursorUtil.getColumnIndexOrThrow(_cursor, "child_ids");
          final int _cursorIndexOfHasAdaptation = CursorUtil.getColumnIndexOrThrow(_cursor, "has_adaptation");
          final int _cursorIndexOfAdaptations = CursorUtil.getColumnIndexOrThrow(_cursor, "adaptations");
          final int _cursorIndexOfImdbUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "imdb_url");
          final int _cursorIndexOfCoverLocalPath = CursorUtil.getColumnIndexOrThrow(_cursor, "cover_local_path");
          final int _cursorIndexOfCoverCandidateUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "cover_candidate_url");
          final int _cursorIndexOfGoodreadsRating = CursorUtil.getColumnIndexOrThrow(_cursor, "goodreads_rating");
          final int _cursorIndexOfGoodreadsRatingsCount = CursorUtil.getColumnIndexOrThrow(_cursor, "goodreads_ratings_count");
          final int _cursorIndexOfIsOwned = CursorUtil.getColumnIndexOrThrow(_cursor, "is_owned");
          final int _cursorIndexOfIsRead = CursorUtil.getColumnIndexOrThrow(_cursor, "is_read");
          final int _cursorIndexOfIsReadingNow = CursorUtil.getColumnIndexOrThrow(_cursor, "is_reading_now");
          final int _cursorIndexOfIsOnReadingList = CursorUtil.getColumnIndexOrThrow(_cursor, "is_on_reading_list");
          final int _cursorIndexOfLastStatusChanged = CursorUtil.getColumnIndexOrThrow(_cursor, "last_status_changed");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final List<BookEntity> _result = new ArrayList<BookEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final BookEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpAuthor;
            _tmpAuthor = _cursor.getString(_cursorIndexOfAuthor);
            final boolean _tmpAsBachman;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfAsBachman);
            _tmpAsBachman = _tmp != 0;
            final Integer _tmpYear;
            if (_cursor.isNull(_cursorIndexOfYear)) {
              _tmpYear = null;
            } else {
              _tmpYear = _cursor.getInt(_cursorIndexOfYear);
            }
            final Integer _tmpDecade;
            if (_cursor.isNull(_cursorIndexOfDecade)) {
              _tmpDecade = null;
            } else {
              _tmpDecade = _cursor.getInt(_cursorIndexOfDecade);
            }
            final Integer _tmpWordCount;
            if (_cursor.isNull(_cursorIndexOfWordCount)) {
              _tmpWordCount = null;
            } else {
              _tmpWordCount = _cursor.getInt(_cursorIndexOfWordCount);
            }
            final Integer _tmpAudibleMinutes;
            if (_cursor.isNull(_cursorIndexOfAudibleMinutes)) {
              _tmpAudibleMinutes = null;
            } else {
              _tmpAudibleMinutes = _cursor.getInt(_cursorIndexOfAudibleMinutes);
            }
            final String _tmpStoryType;
            _tmpStoryType = _cursor.getString(_cursorIndexOfStoryType);
            final List<String> _tmpKeywords;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfKeywords);
            _tmpKeywords = __converters.fromStringList(_tmp_1);
            final List<String> _tmpGenres;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfGenres);
            _tmpGenres = __converters.fromStringList(_tmp_2);
            final boolean _tmpIsCollectionParent;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsCollectionParent);
            _tmpIsCollectionParent = _tmp_3 != 0;
            final String _tmpCollection;
            if (_cursor.isNull(_cursorIndexOfCollection)) {
              _tmpCollection = null;
            } else {
              _tmpCollection = _cursor.getString(_cursorIndexOfCollection);
            }
            final Integer _tmpCollectionId;
            if (_cursor.isNull(_cursorIndexOfCollectionId)) {
              _tmpCollectionId = null;
            } else {
              _tmpCollectionId = _cursor.getInt(_cursorIndexOfCollectionId);
            }
            final List<Integer> _tmpChildIds;
            final String _tmp_4;
            _tmp_4 = _cursor.getString(_cursorIndexOfChildIds);
            _tmpChildIds = __converters.fromIntList(_tmp_4);
            final boolean _tmpHasAdaptation;
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfHasAdaptation);
            _tmpHasAdaptation = _tmp_5 != 0;
            final List<Adaptation> _tmpAdaptations;
            final String _tmp_6;
            _tmp_6 = _cursor.getString(_cursorIndexOfAdaptations);
            _tmpAdaptations = __converters.fromAdaptationList(_tmp_6);
            final String _tmpImdbUrl;
            if (_cursor.isNull(_cursorIndexOfImdbUrl)) {
              _tmpImdbUrl = null;
            } else {
              _tmpImdbUrl = _cursor.getString(_cursorIndexOfImdbUrl);
            }
            final String _tmpCoverLocalPath;
            if (_cursor.isNull(_cursorIndexOfCoverLocalPath)) {
              _tmpCoverLocalPath = null;
            } else {
              _tmpCoverLocalPath = _cursor.getString(_cursorIndexOfCoverLocalPath);
            }
            final String _tmpCoverCandidateUrl;
            if (_cursor.isNull(_cursorIndexOfCoverCandidateUrl)) {
              _tmpCoverCandidateUrl = null;
            } else {
              _tmpCoverCandidateUrl = _cursor.getString(_cursorIndexOfCoverCandidateUrl);
            }
            final Float _tmpGoodreadsRating;
            if (_cursor.isNull(_cursorIndexOfGoodreadsRating)) {
              _tmpGoodreadsRating = null;
            } else {
              _tmpGoodreadsRating = _cursor.getFloat(_cursorIndexOfGoodreadsRating);
            }
            final Integer _tmpGoodreadsRatingsCount;
            if (_cursor.isNull(_cursorIndexOfGoodreadsRatingsCount)) {
              _tmpGoodreadsRatingsCount = null;
            } else {
              _tmpGoodreadsRatingsCount = _cursor.getInt(_cursorIndexOfGoodreadsRatingsCount);
            }
            final boolean _tmpIsOwned;
            final int _tmp_7;
            _tmp_7 = _cursor.getInt(_cursorIndexOfIsOwned);
            _tmpIsOwned = _tmp_7 != 0;
            final boolean _tmpIsRead;
            final int _tmp_8;
            _tmp_8 = _cursor.getInt(_cursorIndexOfIsRead);
            _tmpIsRead = _tmp_8 != 0;
            final boolean _tmpIsReadingNow;
            final int _tmp_9;
            _tmp_9 = _cursor.getInt(_cursorIndexOfIsReadingNow);
            _tmpIsReadingNow = _tmp_9 != 0;
            final boolean _tmpIsOnReadingList;
            final int _tmp_10;
            _tmp_10 = _cursor.getInt(_cursorIndexOfIsOnReadingList);
            _tmpIsOnReadingList = _tmp_10 != 0;
            final Long _tmpLastStatusChanged;
            if (_cursor.isNull(_cursorIndexOfLastStatusChanged)) {
              _tmpLastStatusChanged = null;
            } else {
              _tmpLastStatusChanged = _cursor.getLong(_cursorIndexOfLastStatusChanged);
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            _item = new BookEntity(_tmpId,_tmpTitle,_tmpAuthor,_tmpAsBachman,_tmpYear,_tmpDecade,_tmpWordCount,_tmpAudibleMinutes,_tmpStoryType,_tmpKeywords,_tmpGenres,_tmpIsCollectionParent,_tmpCollection,_tmpCollectionId,_tmpChildIds,_tmpHasAdaptation,_tmpAdaptations,_tmpImdbUrl,_tmpCoverLocalPath,_tmpCoverCandidateUrl,_tmpGoodreadsRating,_tmpGoodreadsRatingsCount,_tmpIsOwned,_tmpIsRead,_tmpIsReadingNow,_tmpIsOnReadingList,_tmpLastStatusChanged,_tmpNotes);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<BookEntity>> observeReadingList() {
    final String _sql = "SELECT * FROM books WHERE is_on_reading_list = 1 ORDER BY last_status_changed ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"books"}, new Callable<List<BookEntity>>() {
      @Override
      @NonNull
      public List<BookEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfAuthor = CursorUtil.getColumnIndexOrThrow(_cursor, "author");
          final int _cursorIndexOfAsBachman = CursorUtil.getColumnIndexOrThrow(_cursor, "as_bachman");
          final int _cursorIndexOfYear = CursorUtil.getColumnIndexOrThrow(_cursor, "year");
          final int _cursorIndexOfDecade = CursorUtil.getColumnIndexOrThrow(_cursor, "decade");
          final int _cursorIndexOfWordCount = CursorUtil.getColumnIndexOrThrow(_cursor, "word_count");
          final int _cursorIndexOfAudibleMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "audible_minutes");
          final int _cursorIndexOfStoryType = CursorUtil.getColumnIndexOrThrow(_cursor, "story_type");
          final int _cursorIndexOfKeywords = CursorUtil.getColumnIndexOrThrow(_cursor, "keywords");
          final int _cursorIndexOfGenres = CursorUtil.getColumnIndexOrThrow(_cursor, "genres");
          final int _cursorIndexOfIsCollectionParent = CursorUtil.getColumnIndexOrThrow(_cursor, "is_collection_parent");
          final int _cursorIndexOfCollection = CursorUtil.getColumnIndexOrThrow(_cursor, "collection");
          final int _cursorIndexOfCollectionId = CursorUtil.getColumnIndexOrThrow(_cursor, "collection_id");
          final int _cursorIndexOfChildIds = CursorUtil.getColumnIndexOrThrow(_cursor, "child_ids");
          final int _cursorIndexOfHasAdaptation = CursorUtil.getColumnIndexOrThrow(_cursor, "has_adaptation");
          final int _cursorIndexOfAdaptations = CursorUtil.getColumnIndexOrThrow(_cursor, "adaptations");
          final int _cursorIndexOfImdbUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "imdb_url");
          final int _cursorIndexOfCoverLocalPath = CursorUtil.getColumnIndexOrThrow(_cursor, "cover_local_path");
          final int _cursorIndexOfCoverCandidateUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "cover_candidate_url");
          final int _cursorIndexOfGoodreadsRating = CursorUtil.getColumnIndexOrThrow(_cursor, "goodreads_rating");
          final int _cursorIndexOfGoodreadsRatingsCount = CursorUtil.getColumnIndexOrThrow(_cursor, "goodreads_ratings_count");
          final int _cursorIndexOfIsOwned = CursorUtil.getColumnIndexOrThrow(_cursor, "is_owned");
          final int _cursorIndexOfIsRead = CursorUtil.getColumnIndexOrThrow(_cursor, "is_read");
          final int _cursorIndexOfIsReadingNow = CursorUtil.getColumnIndexOrThrow(_cursor, "is_reading_now");
          final int _cursorIndexOfIsOnReadingList = CursorUtil.getColumnIndexOrThrow(_cursor, "is_on_reading_list");
          final int _cursorIndexOfLastStatusChanged = CursorUtil.getColumnIndexOrThrow(_cursor, "last_status_changed");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final List<BookEntity> _result = new ArrayList<BookEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final BookEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpAuthor;
            _tmpAuthor = _cursor.getString(_cursorIndexOfAuthor);
            final boolean _tmpAsBachman;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfAsBachman);
            _tmpAsBachman = _tmp != 0;
            final Integer _tmpYear;
            if (_cursor.isNull(_cursorIndexOfYear)) {
              _tmpYear = null;
            } else {
              _tmpYear = _cursor.getInt(_cursorIndexOfYear);
            }
            final Integer _tmpDecade;
            if (_cursor.isNull(_cursorIndexOfDecade)) {
              _tmpDecade = null;
            } else {
              _tmpDecade = _cursor.getInt(_cursorIndexOfDecade);
            }
            final Integer _tmpWordCount;
            if (_cursor.isNull(_cursorIndexOfWordCount)) {
              _tmpWordCount = null;
            } else {
              _tmpWordCount = _cursor.getInt(_cursorIndexOfWordCount);
            }
            final Integer _tmpAudibleMinutes;
            if (_cursor.isNull(_cursorIndexOfAudibleMinutes)) {
              _tmpAudibleMinutes = null;
            } else {
              _tmpAudibleMinutes = _cursor.getInt(_cursorIndexOfAudibleMinutes);
            }
            final String _tmpStoryType;
            _tmpStoryType = _cursor.getString(_cursorIndexOfStoryType);
            final List<String> _tmpKeywords;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfKeywords);
            _tmpKeywords = __converters.fromStringList(_tmp_1);
            final List<String> _tmpGenres;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfGenres);
            _tmpGenres = __converters.fromStringList(_tmp_2);
            final boolean _tmpIsCollectionParent;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsCollectionParent);
            _tmpIsCollectionParent = _tmp_3 != 0;
            final String _tmpCollection;
            if (_cursor.isNull(_cursorIndexOfCollection)) {
              _tmpCollection = null;
            } else {
              _tmpCollection = _cursor.getString(_cursorIndexOfCollection);
            }
            final Integer _tmpCollectionId;
            if (_cursor.isNull(_cursorIndexOfCollectionId)) {
              _tmpCollectionId = null;
            } else {
              _tmpCollectionId = _cursor.getInt(_cursorIndexOfCollectionId);
            }
            final List<Integer> _tmpChildIds;
            final String _tmp_4;
            _tmp_4 = _cursor.getString(_cursorIndexOfChildIds);
            _tmpChildIds = __converters.fromIntList(_tmp_4);
            final boolean _tmpHasAdaptation;
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfHasAdaptation);
            _tmpHasAdaptation = _tmp_5 != 0;
            final List<Adaptation> _tmpAdaptations;
            final String _tmp_6;
            _tmp_6 = _cursor.getString(_cursorIndexOfAdaptations);
            _tmpAdaptations = __converters.fromAdaptationList(_tmp_6);
            final String _tmpImdbUrl;
            if (_cursor.isNull(_cursorIndexOfImdbUrl)) {
              _tmpImdbUrl = null;
            } else {
              _tmpImdbUrl = _cursor.getString(_cursorIndexOfImdbUrl);
            }
            final String _tmpCoverLocalPath;
            if (_cursor.isNull(_cursorIndexOfCoverLocalPath)) {
              _tmpCoverLocalPath = null;
            } else {
              _tmpCoverLocalPath = _cursor.getString(_cursorIndexOfCoverLocalPath);
            }
            final String _tmpCoverCandidateUrl;
            if (_cursor.isNull(_cursorIndexOfCoverCandidateUrl)) {
              _tmpCoverCandidateUrl = null;
            } else {
              _tmpCoverCandidateUrl = _cursor.getString(_cursorIndexOfCoverCandidateUrl);
            }
            final Float _tmpGoodreadsRating;
            if (_cursor.isNull(_cursorIndexOfGoodreadsRating)) {
              _tmpGoodreadsRating = null;
            } else {
              _tmpGoodreadsRating = _cursor.getFloat(_cursorIndexOfGoodreadsRating);
            }
            final Integer _tmpGoodreadsRatingsCount;
            if (_cursor.isNull(_cursorIndexOfGoodreadsRatingsCount)) {
              _tmpGoodreadsRatingsCount = null;
            } else {
              _tmpGoodreadsRatingsCount = _cursor.getInt(_cursorIndexOfGoodreadsRatingsCount);
            }
            final boolean _tmpIsOwned;
            final int _tmp_7;
            _tmp_7 = _cursor.getInt(_cursorIndexOfIsOwned);
            _tmpIsOwned = _tmp_7 != 0;
            final boolean _tmpIsRead;
            final int _tmp_8;
            _tmp_8 = _cursor.getInt(_cursorIndexOfIsRead);
            _tmpIsRead = _tmp_8 != 0;
            final boolean _tmpIsReadingNow;
            final int _tmp_9;
            _tmp_9 = _cursor.getInt(_cursorIndexOfIsReadingNow);
            _tmpIsReadingNow = _tmp_9 != 0;
            final boolean _tmpIsOnReadingList;
            final int _tmp_10;
            _tmp_10 = _cursor.getInt(_cursorIndexOfIsOnReadingList);
            _tmpIsOnReadingList = _tmp_10 != 0;
            final Long _tmpLastStatusChanged;
            if (_cursor.isNull(_cursorIndexOfLastStatusChanged)) {
              _tmpLastStatusChanged = null;
            } else {
              _tmpLastStatusChanged = _cursor.getLong(_cursorIndexOfLastStatusChanged);
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            _item = new BookEntity(_tmpId,_tmpTitle,_tmpAuthor,_tmpAsBachman,_tmpYear,_tmpDecade,_tmpWordCount,_tmpAudibleMinutes,_tmpStoryType,_tmpKeywords,_tmpGenres,_tmpIsCollectionParent,_tmpCollection,_tmpCollectionId,_tmpChildIds,_tmpHasAdaptation,_tmpAdaptations,_tmpImdbUrl,_tmpCoverLocalPath,_tmpCoverCandidateUrl,_tmpGoodreadsRating,_tmpGoodreadsRatingsCount,_tmpIsOwned,_tmpIsRead,_tmpIsReadingNow,_tmpIsOnReadingList,_tmpLastStatusChanged,_tmpNotes);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object count(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM books";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<String>> observeStoryTypes() {
    final String _sql = "SELECT DISTINCT story_type FROM books WHERE author IN ('Stephen King', 'Richard Bachman') ORDER BY story_type ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"books"}, new Callable<List<String>>() {
      @Override
      @NonNull
      public List<String> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final List<String> _result = new ArrayList<String>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final String _item;
            _item = _cursor.getString(0);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<Integer>> observeDecades() {
    final String _sql = "SELECT DISTINCT decade FROM books WHERE author IN ('Stephen King', 'Richard Bachman') AND decade IS NOT NULL ORDER BY decade ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"books"}, new Callable<List<Integer>>() {
      @Override
      @NonNull
      public List<Integer> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final List<Integer> _result = new ArrayList<Integer>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Integer _item;
            _item = _cursor.getInt(0);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<BookEntity> observeById(final int id) {
    final String _sql = "SELECT * FROM books WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"books"}, new Callable<BookEntity>() {
      @Override
      @Nullable
      public BookEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfAuthor = CursorUtil.getColumnIndexOrThrow(_cursor, "author");
          final int _cursorIndexOfAsBachman = CursorUtil.getColumnIndexOrThrow(_cursor, "as_bachman");
          final int _cursorIndexOfYear = CursorUtil.getColumnIndexOrThrow(_cursor, "year");
          final int _cursorIndexOfDecade = CursorUtil.getColumnIndexOrThrow(_cursor, "decade");
          final int _cursorIndexOfWordCount = CursorUtil.getColumnIndexOrThrow(_cursor, "word_count");
          final int _cursorIndexOfAudibleMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "audible_minutes");
          final int _cursorIndexOfStoryType = CursorUtil.getColumnIndexOrThrow(_cursor, "story_type");
          final int _cursorIndexOfKeywords = CursorUtil.getColumnIndexOrThrow(_cursor, "keywords");
          final int _cursorIndexOfGenres = CursorUtil.getColumnIndexOrThrow(_cursor, "genres");
          final int _cursorIndexOfIsCollectionParent = CursorUtil.getColumnIndexOrThrow(_cursor, "is_collection_parent");
          final int _cursorIndexOfCollection = CursorUtil.getColumnIndexOrThrow(_cursor, "collection");
          final int _cursorIndexOfCollectionId = CursorUtil.getColumnIndexOrThrow(_cursor, "collection_id");
          final int _cursorIndexOfChildIds = CursorUtil.getColumnIndexOrThrow(_cursor, "child_ids");
          final int _cursorIndexOfHasAdaptation = CursorUtil.getColumnIndexOrThrow(_cursor, "has_adaptation");
          final int _cursorIndexOfAdaptations = CursorUtil.getColumnIndexOrThrow(_cursor, "adaptations");
          final int _cursorIndexOfImdbUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "imdb_url");
          final int _cursorIndexOfCoverLocalPath = CursorUtil.getColumnIndexOrThrow(_cursor, "cover_local_path");
          final int _cursorIndexOfCoverCandidateUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "cover_candidate_url");
          final int _cursorIndexOfGoodreadsRating = CursorUtil.getColumnIndexOrThrow(_cursor, "goodreads_rating");
          final int _cursorIndexOfGoodreadsRatingsCount = CursorUtil.getColumnIndexOrThrow(_cursor, "goodreads_ratings_count");
          final int _cursorIndexOfIsOwned = CursorUtil.getColumnIndexOrThrow(_cursor, "is_owned");
          final int _cursorIndexOfIsRead = CursorUtil.getColumnIndexOrThrow(_cursor, "is_read");
          final int _cursorIndexOfIsReadingNow = CursorUtil.getColumnIndexOrThrow(_cursor, "is_reading_now");
          final int _cursorIndexOfIsOnReadingList = CursorUtil.getColumnIndexOrThrow(_cursor, "is_on_reading_list");
          final int _cursorIndexOfLastStatusChanged = CursorUtil.getColumnIndexOrThrow(_cursor, "last_status_changed");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final BookEntity _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpAuthor;
            _tmpAuthor = _cursor.getString(_cursorIndexOfAuthor);
            final boolean _tmpAsBachman;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfAsBachman);
            _tmpAsBachman = _tmp != 0;
            final Integer _tmpYear;
            if (_cursor.isNull(_cursorIndexOfYear)) {
              _tmpYear = null;
            } else {
              _tmpYear = _cursor.getInt(_cursorIndexOfYear);
            }
            final Integer _tmpDecade;
            if (_cursor.isNull(_cursorIndexOfDecade)) {
              _tmpDecade = null;
            } else {
              _tmpDecade = _cursor.getInt(_cursorIndexOfDecade);
            }
            final Integer _tmpWordCount;
            if (_cursor.isNull(_cursorIndexOfWordCount)) {
              _tmpWordCount = null;
            } else {
              _tmpWordCount = _cursor.getInt(_cursorIndexOfWordCount);
            }
            final Integer _tmpAudibleMinutes;
            if (_cursor.isNull(_cursorIndexOfAudibleMinutes)) {
              _tmpAudibleMinutes = null;
            } else {
              _tmpAudibleMinutes = _cursor.getInt(_cursorIndexOfAudibleMinutes);
            }
            final String _tmpStoryType;
            _tmpStoryType = _cursor.getString(_cursorIndexOfStoryType);
            final List<String> _tmpKeywords;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfKeywords);
            _tmpKeywords = __converters.fromStringList(_tmp_1);
            final List<String> _tmpGenres;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfGenres);
            _tmpGenres = __converters.fromStringList(_tmp_2);
            final boolean _tmpIsCollectionParent;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsCollectionParent);
            _tmpIsCollectionParent = _tmp_3 != 0;
            final String _tmpCollection;
            if (_cursor.isNull(_cursorIndexOfCollection)) {
              _tmpCollection = null;
            } else {
              _tmpCollection = _cursor.getString(_cursorIndexOfCollection);
            }
            final Integer _tmpCollectionId;
            if (_cursor.isNull(_cursorIndexOfCollectionId)) {
              _tmpCollectionId = null;
            } else {
              _tmpCollectionId = _cursor.getInt(_cursorIndexOfCollectionId);
            }
            final List<Integer> _tmpChildIds;
            final String _tmp_4;
            _tmp_4 = _cursor.getString(_cursorIndexOfChildIds);
            _tmpChildIds = __converters.fromIntList(_tmp_4);
            final boolean _tmpHasAdaptation;
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfHasAdaptation);
            _tmpHasAdaptation = _tmp_5 != 0;
            final List<Adaptation> _tmpAdaptations;
            final String _tmp_6;
            _tmp_6 = _cursor.getString(_cursorIndexOfAdaptations);
            _tmpAdaptations = __converters.fromAdaptationList(_tmp_6);
            final String _tmpImdbUrl;
            if (_cursor.isNull(_cursorIndexOfImdbUrl)) {
              _tmpImdbUrl = null;
            } else {
              _tmpImdbUrl = _cursor.getString(_cursorIndexOfImdbUrl);
            }
            final String _tmpCoverLocalPath;
            if (_cursor.isNull(_cursorIndexOfCoverLocalPath)) {
              _tmpCoverLocalPath = null;
            } else {
              _tmpCoverLocalPath = _cursor.getString(_cursorIndexOfCoverLocalPath);
            }
            final String _tmpCoverCandidateUrl;
            if (_cursor.isNull(_cursorIndexOfCoverCandidateUrl)) {
              _tmpCoverCandidateUrl = null;
            } else {
              _tmpCoverCandidateUrl = _cursor.getString(_cursorIndexOfCoverCandidateUrl);
            }
            final Float _tmpGoodreadsRating;
            if (_cursor.isNull(_cursorIndexOfGoodreadsRating)) {
              _tmpGoodreadsRating = null;
            } else {
              _tmpGoodreadsRating = _cursor.getFloat(_cursorIndexOfGoodreadsRating);
            }
            final Integer _tmpGoodreadsRatingsCount;
            if (_cursor.isNull(_cursorIndexOfGoodreadsRatingsCount)) {
              _tmpGoodreadsRatingsCount = null;
            } else {
              _tmpGoodreadsRatingsCount = _cursor.getInt(_cursorIndexOfGoodreadsRatingsCount);
            }
            final boolean _tmpIsOwned;
            final int _tmp_7;
            _tmp_7 = _cursor.getInt(_cursorIndexOfIsOwned);
            _tmpIsOwned = _tmp_7 != 0;
            final boolean _tmpIsRead;
            final int _tmp_8;
            _tmp_8 = _cursor.getInt(_cursorIndexOfIsRead);
            _tmpIsRead = _tmp_8 != 0;
            final boolean _tmpIsReadingNow;
            final int _tmp_9;
            _tmp_9 = _cursor.getInt(_cursorIndexOfIsReadingNow);
            _tmpIsReadingNow = _tmp_9 != 0;
            final boolean _tmpIsOnReadingList;
            final int _tmp_10;
            _tmp_10 = _cursor.getInt(_cursorIndexOfIsOnReadingList);
            _tmpIsOnReadingList = _tmp_10 != 0;
            final Long _tmpLastStatusChanged;
            if (_cursor.isNull(_cursorIndexOfLastStatusChanged)) {
              _tmpLastStatusChanged = null;
            } else {
              _tmpLastStatusChanged = _cursor.getLong(_cursorIndexOfLastStatusChanged);
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            _result = new BookEntity(_tmpId,_tmpTitle,_tmpAuthor,_tmpAsBachman,_tmpYear,_tmpDecade,_tmpWordCount,_tmpAudibleMinutes,_tmpStoryType,_tmpKeywords,_tmpGenres,_tmpIsCollectionParent,_tmpCollection,_tmpCollectionId,_tmpChildIds,_tmpHasAdaptation,_tmpAdaptations,_tmpImdbUrl,_tmpCoverLocalPath,_tmpCoverCandidateUrl,_tmpGoodreadsRating,_tmpGoodreadsRatingsCount,_tmpIsOwned,_tmpIsRead,_tmpIsReadingNow,_tmpIsOnReadingList,_tmpLastStatusChanged,_tmpNotes);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<BookEntity>> observeByIds(final List<Integer> ids) {
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT * FROM books WHERE id IN (");
    final int _inputSize = ids.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(") ORDER BY year ASC, title ASC");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 0 + _inputSize;
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    for (int _item : ids) {
      _statement.bindLong(_argIndex, _item);
      _argIndex++;
    }
    return CoroutinesRoom.createFlow(__db, false, new String[] {"books"}, new Callable<List<BookEntity>>() {
      @Override
      @NonNull
      public List<BookEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfAuthor = CursorUtil.getColumnIndexOrThrow(_cursor, "author");
          final int _cursorIndexOfAsBachman = CursorUtil.getColumnIndexOrThrow(_cursor, "as_bachman");
          final int _cursorIndexOfYear = CursorUtil.getColumnIndexOrThrow(_cursor, "year");
          final int _cursorIndexOfDecade = CursorUtil.getColumnIndexOrThrow(_cursor, "decade");
          final int _cursorIndexOfWordCount = CursorUtil.getColumnIndexOrThrow(_cursor, "word_count");
          final int _cursorIndexOfAudibleMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "audible_minutes");
          final int _cursorIndexOfStoryType = CursorUtil.getColumnIndexOrThrow(_cursor, "story_type");
          final int _cursorIndexOfKeywords = CursorUtil.getColumnIndexOrThrow(_cursor, "keywords");
          final int _cursorIndexOfGenres = CursorUtil.getColumnIndexOrThrow(_cursor, "genres");
          final int _cursorIndexOfIsCollectionParent = CursorUtil.getColumnIndexOrThrow(_cursor, "is_collection_parent");
          final int _cursorIndexOfCollection = CursorUtil.getColumnIndexOrThrow(_cursor, "collection");
          final int _cursorIndexOfCollectionId = CursorUtil.getColumnIndexOrThrow(_cursor, "collection_id");
          final int _cursorIndexOfChildIds = CursorUtil.getColumnIndexOrThrow(_cursor, "child_ids");
          final int _cursorIndexOfHasAdaptation = CursorUtil.getColumnIndexOrThrow(_cursor, "has_adaptation");
          final int _cursorIndexOfAdaptations = CursorUtil.getColumnIndexOrThrow(_cursor, "adaptations");
          final int _cursorIndexOfImdbUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "imdb_url");
          final int _cursorIndexOfCoverLocalPath = CursorUtil.getColumnIndexOrThrow(_cursor, "cover_local_path");
          final int _cursorIndexOfCoverCandidateUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "cover_candidate_url");
          final int _cursorIndexOfGoodreadsRating = CursorUtil.getColumnIndexOrThrow(_cursor, "goodreads_rating");
          final int _cursorIndexOfGoodreadsRatingsCount = CursorUtil.getColumnIndexOrThrow(_cursor, "goodreads_ratings_count");
          final int _cursorIndexOfIsOwned = CursorUtil.getColumnIndexOrThrow(_cursor, "is_owned");
          final int _cursorIndexOfIsRead = CursorUtil.getColumnIndexOrThrow(_cursor, "is_read");
          final int _cursorIndexOfIsReadingNow = CursorUtil.getColumnIndexOrThrow(_cursor, "is_reading_now");
          final int _cursorIndexOfIsOnReadingList = CursorUtil.getColumnIndexOrThrow(_cursor, "is_on_reading_list");
          final int _cursorIndexOfLastStatusChanged = CursorUtil.getColumnIndexOrThrow(_cursor, "last_status_changed");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final List<BookEntity> _result = new ArrayList<BookEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final BookEntity _item_1;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpAuthor;
            _tmpAuthor = _cursor.getString(_cursorIndexOfAuthor);
            final boolean _tmpAsBachman;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfAsBachman);
            _tmpAsBachman = _tmp != 0;
            final Integer _tmpYear;
            if (_cursor.isNull(_cursorIndexOfYear)) {
              _tmpYear = null;
            } else {
              _tmpYear = _cursor.getInt(_cursorIndexOfYear);
            }
            final Integer _tmpDecade;
            if (_cursor.isNull(_cursorIndexOfDecade)) {
              _tmpDecade = null;
            } else {
              _tmpDecade = _cursor.getInt(_cursorIndexOfDecade);
            }
            final Integer _tmpWordCount;
            if (_cursor.isNull(_cursorIndexOfWordCount)) {
              _tmpWordCount = null;
            } else {
              _tmpWordCount = _cursor.getInt(_cursorIndexOfWordCount);
            }
            final Integer _tmpAudibleMinutes;
            if (_cursor.isNull(_cursorIndexOfAudibleMinutes)) {
              _tmpAudibleMinutes = null;
            } else {
              _tmpAudibleMinutes = _cursor.getInt(_cursorIndexOfAudibleMinutes);
            }
            final String _tmpStoryType;
            _tmpStoryType = _cursor.getString(_cursorIndexOfStoryType);
            final List<String> _tmpKeywords;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfKeywords);
            _tmpKeywords = __converters.fromStringList(_tmp_1);
            final List<String> _tmpGenres;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfGenres);
            _tmpGenres = __converters.fromStringList(_tmp_2);
            final boolean _tmpIsCollectionParent;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsCollectionParent);
            _tmpIsCollectionParent = _tmp_3 != 0;
            final String _tmpCollection;
            if (_cursor.isNull(_cursorIndexOfCollection)) {
              _tmpCollection = null;
            } else {
              _tmpCollection = _cursor.getString(_cursorIndexOfCollection);
            }
            final Integer _tmpCollectionId;
            if (_cursor.isNull(_cursorIndexOfCollectionId)) {
              _tmpCollectionId = null;
            } else {
              _tmpCollectionId = _cursor.getInt(_cursorIndexOfCollectionId);
            }
            final List<Integer> _tmpChildIds;
            final String _tmp_4;
            _tmp_4 = _cursor.getString(_cursorIndexOfChildIds);
            _tmpChildIds = __converters.fromIntList(_tmp_4);
            final boolean _tmpHasAdaptation;
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfHasAdaptation);
            _tmpHasAdaptation = _tmp_5 != 0;
            final List<Adaptation> _tmpAdaptations;
            final String _tmp_6;
            _tmp_6 = _cursor.getString(_cursorIndexOfAdaptations);
            _tmpAdaptations = __converters.fromAdaptationList(_tmp_6);
            final String _tmpImdbUrl;
            if (_cursor.isNull(_cursorIndexOfImdbUrl)) {
              _tmpImdbUrl = null;
            } else {
              _tmpImdbUrl = _cursor.getString(_cursorIndexOfImdbUrl);
            }
            final String _tmpCoverLocalPath;
            if (_cursor.isNull(_cursorIndexOfCoverLocalPath)) {
              _tmpCoverLocalPath = null;
            } else {
              _tmpCoverLocalPath = _cursor.getString(_cursorIndexOfCoverLocalPath);
            }
            final String _tmpCoverCandidateUrl;
            if (_cursor.isNull(_cursorIndexOfCoverCandidateUrl)) {
              _tmpCoverCandidateUrl = null;
            } else {
              _tmpCoverCandidateUrl = _cursor.getString(_cursorIndexOfCoverCandidateUrl);
            }
            final Float _tmpGoodreadsRating;
            if (_cursor.isNull(_cursorIndexOfGoodreadsRating)) {
              _tmpGoodreadsRating = null;
            } else {
              _tmpGoodreadsRating = _cursor.getFloat(_cursorIndexOfGoodreadsRating);
            }
            final Integer _tmpGoodreadsRatingsCount;
            if (_cursor.isNull(_cursorIndexOfGoodreadsRatingsCount)) {
              _tmpGoodreadsRatingsCount = null;
            } else {
              _tmpGoodreadsRatingsCount = _cursor.getInt(_cursorIndexOfGoodreadsRatingsCount);
            }
            final boolean _tmpIsOwned;
            final int _tmp_7;
            _tmp_7 = _cursor.getInt(_cursorIndexOfIsOwned);
            _tmpIsOwned = _tmp_7 != 0;
            final boolean _tmpIsRead;
            final int _tmp_8;
            _tmp_8 = _cursor.getInt(_cursorIndexOfIsRead);
            _tmpIsRead = _tmp_8 != 0;
            final boolean _tmpIsReadingNow;
            final int _tmp_9;
            _tmp_9 = _cursor.getInt(_cursorIndexOfIsReadingNow);
            _tmpIsReadingNow = _tmp_9 != 0;
            final boolean _tmpIsOnReadingList;
            final int _tmp_10;
            _tmp_10 = _cursor.getInt(_cursorIndexOfIsOnReadingList);
            _tmpIsOnReadingList = _tmp_10 != 0;
            final Long _tmpLastStatusChanged;
            if (_cursor.isNull(_cursorIndexOfLastStatusChanged)) {
              _tmpLastStatusChanged = null;
            } else {
              _tmpLastStatusChanged = _cursor.getLong(_cursorIndexOfLastStatusChanged);
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            _item_1 = new BookEntity(_tmpId,_tmpTitle,_tmpAuthor,_tmpAsBachman,_tmpYear,_tmpDecade,_tmpWordCount,_tmpAudibleMinutes,_tmpStoryType,_tmpKeywords,_tmpGenres,_tmpIsCollectionParent,_tmpCollection,_tmpCollectionId,_tmpChildIds,_tmpHasAdaptation,_tmpAdaptations,_tmpImdbUrl,_tmpCoverLocalPath,_tmpCoverCandidateUrl,_tmpGoodreadsRating,_tmpGoodreadsRatingsCount,_tmpIsOwned,_tmpIsRead,_tmpIsReadingNow,_tmpIsOnReadingList,_tmpLastStatusChanged,_tmpNotes);
            _result.add(_item_1);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}

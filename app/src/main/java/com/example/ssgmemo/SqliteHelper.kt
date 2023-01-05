package com.example.ssgmemo

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class SqliteHelper(context: Context, name: String, version: Int):
	SQLiteOpenHelper(context, name, null, version) {
	// SQLiteOpenHelper : DB를 생성하고, 코틀린으로 DB를 사용할 수 있도록 연결하는 역할
	override fun onCreate(db: SQLiteDatabase?) {
		// 앱이 설치되어 SQLiteOpenHelper 클래스가 최초로 사용되는 순간 호출됨
		// 전체 앱에서 가장 처음 한 번만 수행되며, 대부분 테이블을 생성하는 코드를 작성
		val sql = "create table ctgr (idx integer primary key, name text, datetime integer)"
		db?.execSQL(sql)
		val sql1 = "create table memo (idx integer primary key, title text default '빈 제목', content text not null, datetime integer, ctgr integer, FOREIGN KEY (ctgr) references ctgr(ctgr) ON UPDATE CASCADE ON DELETE CASCADE)"
		db?.execSQL(sql1)
	}
	override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) { }
	// DB 버전 정보가 변경될 때 마다 반복해서 호출되며, 테이블의 스키마 부분을 변경하기 위한 용도로 사용

	fun insertCtgr(ctgr: Ctgr) {
		val wd = writableDatabase
		val sql = "insert into ctgr (name, datetime) values " +
				"('${ctgr.name}', ${ctgr.datetime})"
		wd.execSQL(sql)
		wd.close()
	}
	fun insertMemo(memo: Memo) {
		val wd = writableDatabase
		val sql = "insert into memo (title,content,datetime,ctgr) values " +
				"('${memo.title}', '${memo.content}', ${memo.datetime}, ${memo.ctgr})"
		wd.execSQL(sql)
		wd.close()
	}

	fun updateCtgr(ctgr: Ctgr) {
		// memo 테이블에 기존 레코드를 받아온 새로운 레코드로 변경하는 함수
		val values = ContentValues()
		values.put("name", ctgr.name)
		values.put("datetime", ctgr.datetime)
		// 테이블에서 변경할 값들을 컬럼명과 함께 저장

		val wd = writableDatabase
		wd.update("ctgr", values, "idx = ${ctgr.idx}", null)
		// wd.update("memo", values, "idx = ?", arrayOf("${memo.idx}")
		wd.close()
	}

	@SuppressLint("Range")
	fun selectCtgrMap(): MutableMap<String,Int> {
		// 카테고리 맵 // 쓰기에서 카테고리 불러올 때 사용
		val map = mutableMapOf<String,Int>()
		val sql = "select name, idx from ctgr "
		val rd = readableDatabase
		val rs = rd.rawQuery(sql, null)

		while (rs.moveToNext()) {
			// moveToNext() : 자바의 next()와 동일한 메소드로 커서를 다음 레코드로 내리면서 데이터 존재여부를 리턴
			val name = rs.getString(rs.getColumnIndex("name"))
			val idx = rs.getLong(rs.getColumnIndex("idx"))
			map.put(name, idx.toInt())
		}
		rs.close()
		rd.close()

		return map
	}

	@SuppressLint("Range")
	fun selectCtgrList(): MutableList<Ctgr> {
		// 카테고리 리스트 // 보기, 분류에서 카테고리 불러올 때 사용
		val list = mutableListOf<Ctgr>()
		val sql = "select * from ctgr"
		val rd = readableDatabase
		val rs = rd.rawQuery(sql, null)

		while (rs.moveToNext()) {
			// moveToNext() : 자바의 next()와 동일한 메소드로 커서를 다음 레코드로 내리면서 데이터 존재여부를 리턴
			val idx = rs.getLong(rs.getColumnIndex("idx"))
			val name = rs.getString(rs.getColumnIndex("name"))
			val datetime = rs.getLong(rs.getColumnIndex("datetime"))
			list.add(Ctgr(idx, name, datetime))
		}
		rs.close()
		rd.close()

		return list
	}

	@SuppressLint("Range")
	fun selectMemoList(): MutableList<Memo> {
		// 메모 리스트 // 보기, 분류에서 메모 불러올 때 사용
		val list = mutableListOf<Memo>()
		val sql = "select * from memo"
		val rd = readableDatabase
		val rs = rd.rawQuery(sql, null)

		while (rs.moveToNext()) {
			// moveToNext() : 자바의 next()와 동일한 메소드로 커서를 다음 레코드로 내리면서 데이터 존재여부를 리턴
			val idx = rs.getLong(rs.getColumnIndex("idx"))
			val title = rs.getString(rs.getColumnIndex("title"))
			val content = rs.getString(rs.getColumnIndex("content"))
			val datetime = rs.getLong(rs.getColumnIndex("datetime"))
			val ctgr = rs.getInt(rs.getColumnIndex("ctgr"))

			list.add(Memo(idx, title, content, datetime, ctgr))
		}
		rs.close()
		rd.close()

		return list
	}
	@SuppressLint("Range")
	fun selectMemoList(ctgr:String): MutableList<Memo> {
		// 메모 리스트 // 보기, 분류에서 메모 불러올 때 사용
		val list = mutableListOf<Memo>()
		Log.d("ctgr","${ctgr}")
		val sql = "select * from memo where ctgr = '"+ ctgr +"' "
		val rd = readableDatabase
		val rs = rd.rawQuery(sql, null)

		while (rs.moveToNext()) {
			// moveToNext() : 자바의 next()와 동일한 메소드로 커서를 다음 레코드로 내리면서 데이터 존재여부를 리턴
			val idx = rs.getLong(rs.getColumnIndex("idx"))
			val title = rs.getString(rs.getColumnIndex("title"))
			val content = rs.getString(rs.getColumnIndex("content"))
			val datetime = rs.getLong(rs.getColumnIndex("datetime"))
			val ctgr = rs.getInt(rs.getColumnIndex("ctgr"))

			list.add(Memo(idx, title, content, datetime, ctgr))
		}
		rs.close()
		rd.close()

		return list
	}

	fun deleteCtgr() {
		//카테고리 삭제 메소드
		val wd = writableDatabase
		val sql = "delete from ctgr"
		wd.execSQL(sql)
		wd.close()
	}

	
}

data class Ctgr(var idx: Long?, var name: String, var datetime: Long)
data class Memo(var idx: Long?, var title: String, var content: String,var datetime: Long, var ctgr: Int?)
// memo 테이블의 레코드 하나를 저장할 수 있는 데이터 클래스
// idx는 primary key이므로 자동증가값으로 설정되어 값이 없을 수도 있으므로 null값을 허용(?)
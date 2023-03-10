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
		val sql1 = "create table memo (idx integer primary key, title text default '빈 제목', content text not null, datetime integer, ctgr integer, priority integer, FOREIGN KEY (ctgr) references ctgr(idx) ON UPDATE CASCADE ON DELETE CASCADE)"
		db?.execSQL(sql1)
	}
	override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) { }
	// DB 버전 정보가 변경될 때 마다 반복해서 호출되며, 테이블의 스키마 부분을 변경하기 위한 용도로 사용

	fun insertCtgr(ctgr: Ctgr) {
		var sql1 = "select idx FROM ctgr where name = '" + ctgr.name + "' LIMIT 1"
		val rd = readableDatabase
		val rs = rd.rawQuery(sql1, null)

		if (!rs.moveToNext()) {
			val wd = writableDatabase
			val sql2 = "insert into ctgr (name, datetime) values " +
					"('${ctgr.name}', ${ctgr.datetime})"
			wd.execSQL(sql2)
			wd.close()
		}
		rs.close()
		rd.close()
	}
	fun insertMemo(memo: Memo) {
		val wd = writableDatabase
		val sql = "insert into memo (title,content,datetime,ctgr,priority) values " +
				"('${memo.title}', '${memo.content}', ${memo.datetime}, ${memo.ctgr}, ${memo.priority})"
		wd.execSQL(sql)
		wd.close()
	}

	fun updateMemoCtgr(memoidx: Long?, ctgr: Long, priority: Int?) {
		// memo 테이블에 기존 레코드를 받아온 새로운 레코드로 변경하는 함수
		val values = ContentValues()
		values.put("ctgr", ctgr)
		values.put("priority", priority)
		// 테이블에서 변경할 값들을 컬럼명과 함께 저장

		val wd = writableDatabase
		wd.update("memo", values, "idx = ${memoidx}", null)
		wd.close()
	}

	// memo 테이블에 기존 레코드를 받아온 새로운 레코드로 변경하는 함수
	fun updateMemo(memo: Memo, diffCtgr: Boolean, ctgr_before: Int, priority_before: Int) {
		// 카테고리가 변경되었다면 우선순위 조정
		if(diffCtgr){
			if (memo.ctgr != null){
				val wd = writableDatabase
				val sql = "UPDATE memo set priority = priority-1 where ctgr = '$ctgr_before' and priority>'$priority_before'"
				wd.execSQL(sql)
				wd.close()
			}
		}
		val db = this.writableDatabase
		val title = memo.title
		val content = memo.content
		val ctgr = memo.ctgr
		val datetime = System.currentTimeMillis()
		var priority = memo.priority
		val contentValues = ContentValues().apply {
			put("title", title)
			put("content", content)
			put("datetime", datetime)
			put("priority",priority)
			put("ctgr",ctgr)
		}
		Log.d("결과","${title}")
		db.update("memo", contentValues, "idx = ${memo.idx}", null)
		db.close()
	}

	fun updateCtgrName(idx: String, title: String) {
		// memo 테이블에 기존 레코드를 받아온 새로운 레코드로 변경하는 함수
		val db = this.writableDatabase
		val contentValues = ContentValues().apply {
			put("name", title)
		}
		Log.d("확인","${idx +"+ " + title}")
		db.update("ctgr", contentValues, "idx = ${idx}", null)
		db.close()
	}

	@SuppressLint("Range")
	fun selectCtgrMap(): MutableMap<Int,String> {
		// 카테고리 맵 // 쓰기에서 카테고리 불러올 때 사용
		val map = mutableMapOf<Int,String>()
		val sql = "select name, idx from ctgr "
		val rd = readableDatabase
		val rs = rd.rawQuery(sql, null)

		while (rs.moveToNext()) {
			// moveToNext() : 자바의 next()와 동일한 메소드로 커서를 다음 레코드로 내리면서 데이터 존재여부를 리턴
			val name = rs.getString(rs.getColumnIndex("name"))
			val idx = rs.getLong(rs.getColumnIndex("idx"))
			map[idx.toInt()] = name
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
	fun selectUnclassifiedMemoList(): MutableList<Memo> {
		// 메모 리스트 // 분류에서 메모 불러올 때 사용
		val list = mutableListOf<Memo>()
		val sql = "select * from memo where ctgr = '0' order by priority asc"
		val rd = readableDatabase
		val rs = rd.rawQuery(sql, null)

		while (rs.moveToNext()) {
			// moveToNext() : 자바의 next()와 동일한 메소드로 커서를 다음 레코드로 내리면서 데이터 존재여부를 리턴
			val idx = rs.getLong(rs.getColumnIndex("idx"))
			val title = rs.getString(rs.getColumnIndex("title"))
			val content = rs.getString(rs.getColumnIndex("content"))
			val datetime = rs.getLong(rs.getColumnIndex("datetime"))
			val ctgr = rs.getInt(rs.getColumnIndex("ctgr"))
			val priority = rs.getInt(rs.getColumnIndex("priority"))

			list.add(Memo(idx, title, content, datetime, ctgr, priority))
		}
		rs.close()
		rd.close()

		return list
	}

	@SuppressLint("Range")
	fun selectMemoList(ctgr:String): MutableList<Memo> {
		// 메모 리스트 // 보기에서 메모 불러올 때 사용
		val list = mutableListOf<Memo>()
		var sql = "select * from memo where ctgr = '"+ ctgr +"' order by priority desc"
		
		val rd = readableDatabase
		val rs = rd.rawQuery(sql, null)

		while (rs.moveToNext()) {
			// moveToNext() : 자바의 next()와 동일한 메소드로 커서를 다음 레코드로 내리면서 데이터 존재여부를 리턴
			val idx = rs.getLong(rs.getColumnIndex("idx"))
			val title = rs.getString(rs.getColumnIndex("title"))
			val content = rs.getString(rs.getColumnIndex("content"))
			val datetime = rs.getLong(rs.getColumnIndex("datetime"))
			val ctgr = rs.getInt(rs.getColumnIndex("ctgr"))
			val priority = rs.getInt(rs.getColumnIndex("priority"))

			list.add(Memo(idx, title, content, datetime, ctgr, priority))
		}
		rs.close()
		rd.close()

		return list
	}

	@SuppressLint("Range")
	fun selectMemo(idx:String): Memo {
		// 메모 // 보기에서 메모 불러올 때 사용
		lateinit var memo:Memo
		var sql = "select * from memo where idx = '"+ idx +"' order by priority desc"
		val rd = readableDatabase
		val rs = rd.rawQuery(sql, null)

		while (rs.moveToNext()) {
			// moveToNext() : 자바의 next()와 동일한 메소드로 커서를 다음 레코드로 내리면서 데이터 존재여부를 리턴
			val idx = rs.getLong(rs.getColumnIndex("idx"))
			val title = rs.getString(rs.getColumnIndex("title"))
			val content = rs.getString(rs.getColumnIndex("content"))
			val datetime = rs.getLong(rs.getColumnIndex("datetime"))
			val ctgr = rs.getInt(rs.getColumnIndex("ctgr"))
			val priority = rs.getInt(rs.getColumnIndex("priority"))
			memo = Memo(idx, title, content, datetime, ctgr, priority)
		}
		rs.close()
		rd.close()

		return memo
	}

	fun deleteCtgr(idx: String) {
		// 우선순위 수정을 위함
		val memoList = selectMemoList(idx)
		var unclassifiedTop:Int = if(checkTopMemo(0)==null){0}else{checkTopMemo(0)!!}
		//관련 메모의 카테고리 삭제 메소드
		val wd = writableDatabase
		val sql1 = "UPDATE memo set ctgr = 0 where ctgr = '" + idx + " '"
		wd.execSQL(sql1)
		// 카테고리 삭제
		val sql2 = "delete from ctgr where idx = '" + idx + " '"
		wd.execSQL(sql2)
		// 기존의 미분류에서 우선순위 설정
		var sql3 = ""
		for (memoList in memoList){
			sql3 = "UPDATE memo set priority = '" + ++unclassifiedTop + "' where idx = '" + memoList.idx + " '"
			wd.execSQL(sql3)
		}
		wd.close()
	}
	// 미분류 메모가 있다면 true 반환
	@SuppressLint("Range")
	fun isUnknownMemoExist(): Boolean{
		var result: Boolean = false
		var flag: Int? = null
		val sql = "select exists(select * from memo where ctgr = 0) as b"
		val rd = readableDatabase
		val rs = rd.rawQuery(sql, null)
		while (rs.moveToNext()) {
			flag = rs.getInt(rs.getColumnIndex("b"))
		}
		if (flag == 1){
			result = true
		}
		rs.close()
		rd.close()
		return result
	}
	@SuppressLint("Range")
	fun isCtgrMemoExist(ctgrIdx:String): Boolean{
		var result: Boolean = false
		var flag: Int? = null
		val sql = "select exists(select * from memo where ctgr = '" + ctgrIdx + "') as b"
		val rd = readableDatabase
		val rs = rd.rawQuery(sql, null)
		while (rs.moveToNext()) {
			flag = rs.getInt(rs.getColumnIndex("b"))
		}
		if (flag == 1){
			result = true
		}
		rs.close()
		rd.close()
		return result
	}

	fun deleteContent(memo: Memo) {
		// 삭제 할 데이터 보다 우선순위가 큰 경우 -1
		if (memo.ctgr != null){
			val wd1 = writableDatabase
			val sql1 = "UPDATE memo set priority = priority-1 where ctgr = '" + memo.ctgr + "' and priority>'" + memo.priority + "'"
			wd1.execSQL(sql1)
			wd1.close()
		}
		// 데이터 삭제
		val wd = writableDatabase
		val sql = "delete from memo where idx = '" + memo.idx + " '"

		wd.execSQL(sql)
		wd.close()

	}

	fun movePriority(itemList_from: Memo, itemList_to: Memo) {
		val wd = writableDatabase
		val wd1 = writableDatabase
		val sql = "UPDATE memo set priority = '" + itemList_from.priority + "' where idx = '" + itemList_from.idx + "'"
		val sql1 = "UPDATE memo set priority = '" + itemList_to.priority + "' where idx = '" + itemList_to.idx + "'"

		wd.execSQL(sql)
		wd1.execSQL(sql1)
		wd.close()
		wd1.close()
	}


	@SuppressLint("Range")
	fun checkTopMemo(ctgr: Int): Int? {

		var sql = "SELECT priority FROM memo where ctgr = '" + ctgr + "' ORDER by priority DESC LIMIT 1"
		val rd = readableDatabase
		val rs = rd.rawQuery(sql, null)
		var result: Int? = null

		if (rs.moveToNext()) {
			// moveToNext() : 자바의 next()와 동일한 메소드로 커서를 다음 레코드로 내리면서 데이터 존재여부를 리턴
			result = rs.getInt(rs.getColumnIndex("priority"))
		}
		rs.close()
		rd.close()
		return  result
	}

	@SuppressLint("Range")
	fun checkTopCtgr(): Int? {
		var sql = "SELECT idx FROM ctgr ORDER by datetime DESC LIMIT 1"
		val rd = readableDatabase
		val rs = rd.rawQuery(sql, null)
		var result = 0

		if (rs.moveToNext()) {
			result = rs.getInt(rs.getColumnIndex("idx"))
		}
		rs.close()
		rd.close()
		return result
	}

	@SuppressLint("Range")
	fun selectSearchList(keyword: String, where: String, orderby: String): MutableList<Memo> {

		val list = mutableListOf<Memo>()
		var condition1 = ""
		var condition2 = ""

		when (where) {
			"제목" -> {
				condition1 = "where title like '%$keyword%'"
			}
			"내용" -> {
				condition1 = "where content like '%$keyword%'"
			}
			"제목+내용" -> {
				condition1 = "where title like '%$keyword%' or content like '%$keyword%'"
			}
		}

		condition2 = if( orderby == "최신순") {
			"order by datetime desc"
		} else {
			"order by datetime asc"
		}


		var sql = "select * from memo $condition1 $condition2"
		val rd = readableDatabase
		val rs = rd.rawQuery(sql, null)

		while (rs.moveToNext()) {
			// moveToNext() : 자바의 next()와 동일한 메소드로 커서를 다음 레코드로 내리면서 데이터 존재여부를 리턴
			val idx = rs.getLong(rs.getColumnIndex("idx"))
			val title = rs.getString(rs.getColumnIndex("title"))
			val content = rs.getString(rs.getColumnIndex("content"))
			val datetime = rs.getLong(rs.getColumnIndex("datetime"))
			val ctgr = rs.getInt(rs.getColumnIndex("ctgr"))
			val priority = rs.getInt(rs.getColumnIndex("priority"))

			list.add(Memo(idx, title, content, datetime, ctgr, priority))
		}
		rs.close()
		rd.close()

		return list
	}

	@SuppressLint("Range")
	fun checkDuplicationCtgr(title: String): Boolean{
		val sql = "select exists(select * from ctgr where name = '" + title + "') as b "
		val rd = readableDatabase
		val rs = rd.rawQuery(sql, null)
		var result: Boolean = false
		var flag: Int = 0
		while (rs.moveToNext()) {
			flag = rs.getInt(rs.getColumnIndex("b"))
		}
		if (flag == 1){
			result = true
		}
		rs.close()
		rd.close()
		return result
	}

	fun deleteMemoCtgr(memoidx: String) {
		val memo = selectMemo(memoidx)
		val priority = if (checkTopMemo(0) == null){ 0 } else{ checkTopMemo(0)!!+1}
		val wd = writableDatabase
		val sql = "update memo set priority = '" + priority + "', ctgr = 0 where idx = '" + memoidx + "'"

		val sql1 = "UPDATE memo set priority = priority-1 where ctgr = '" + memo.ctgr + "' and priority>'" + memo.priority + "'"
		wd.execSQL(sql1)
		wd.execSQL(sql)

		wd.close()
	}

    fun deleteMemoFromCtgr(cidx: String) {
		val wd = writableDatabase
		val sql = "delete from memo where ctgr = '" + cidx + "'"
		wd.execSQL(sql)
		wd.close()
    }

	@SuppressLint("Range")
	fun getTopPriority(ctgr: Int?) : Int {
		val sql = "select priority from memo where ctgr = '" + ctgr + "' order by priority desc limit 1"
		val rd = readableDatabase
		val rs = rd.rawQuery(sql, null)
		var result: Int = 0
		if (rs.moveToNext()) {
			result = rs.getInt(rs.getColumnIndex("priority"))
		}
		rs.close()
		rd.close()
		return result
	}

	@SuppressLint("Range")
	fun updatePriority(cidx: Long?) {
		val sql1 = "select idx from memo where ctgr = '" + cidx + "' order by priority"
		val rd = readableDatabase
		val rs = rd.rawQuery(sql1, null)
		var priority = 0
		val wd = writableDatabase
		while (rs.moveToNext()) {
			val sql2 = "update memo set priority = '" + priority + "', ctgr = " + cidx + " where idx = '" + rs.getLong(rs.getColumnIndex("idx")) + "'"
			wd.execSQL(sql2)
			priority ++
		}
		wd.close()
		rs.close()
		rd.close()
	}

    @SuppressLint("Range")
	fun selectCtgrName(memoCtgr: String?): String {
		var sql = "select name from ctgr where idx = '"+ memoCtgr +"'"
		val rd = readableDatabase
		val rs = rd.rawQuery(sql, null)
		var name : String = ""

		if (rs.moveToNext()) {
			name = rs.getString(rs.getColumnIndex("name"))
		}
		rs.close()
		rd.close()

		return name
    }

	fun moveContent(memo: Memo, ctgr: Long) {

		if (memo.ctgr != null){
			val wd1 = writableDatabase
			val sql1 = "UPDATE memo set priority = priority-1 where ctgr = '" + memo.ctgr + "' and priority>'" + memo.priority + "'"
			wd1.execSQL(sql1)
			wd1.close()
		}
		// 데이터 이동
		updateMemoCtgr(memo.idx, ctgr, getTopPriority(ctgr.toInt()) + 1)
	}

}

data class Ctgr(var idx: Long?, var name: String, var datetime: Long)
data class Memo(var idx: Long?, var title: String, var content: String, var datetime: Long, var ctgr: Int, var priority: Int, var sel :Boolean = false)
// memo 테이블의 레코드 하나를 저장할 수 있는 데이터 클래스
// idx는 primary key이므로 자동증가값으로 설정되어 값이 없을 수도 있으므로 null값을 허용(?)
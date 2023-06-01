package org.herodbx.util;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author 顾法华
 * @since 2022年03月02日
 */
public class HeroJdbcTool {

  private static final Logger logger = Logger.getLogger(HeroUtils.class.getName());

  @FunctionalInterface
  static public interface Callback4TestTool{
    public void Callback(ResultSet rs,int columnIdx,StringBuffer sb)throws SQLException;
  }

  static public void executeSql(String url,String sql)throws ClassNotFoundException, SQLException{
    executeSql(url,sql,null);
  }

  static public void executeSql(String url,String sql,Callback4TestTool callback)throws ClassNotFoundException, SQLException{
    List<String> sqlList = new ArrayList<>();
    sqlList.add(sql);
    executeSqlList(url,sqlList,callback);
  }

  static public void executeSqlList(String url,List<String> sqlList)throws ClassNotFoundException, SQLException{
    executeSqlList(url,sqlList,null);
  }

  static public void executeSqlList(String url,List<String> sqlList,Callback4TestTool callback)throws ClassNotFoundException, SQLException{
    executeSqlcore(url,sqlList,callback);
  }

  static private void executeSqlcore(String url, List<String> sqlList, Callback4TestTool callback) throws ClassNotFoundException, SQLException {
    Connection conn = DriverManager.getConnection(url);
    Statement st = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);

    for(String sql:sqlList){
      boolean b = st.execute(sql);
      if (b){
        do{//返回的第一个结果是一个 ResultSet
          ResultSet rs = st.getResultSet();
          displayResultSet(rs,callback);
        }while(st.getMoreResults());
      }
    }
    st.close();
    conn.close();
  }

  static private void displayResultSet(ResultSet rs, Callback4TestTool callback) throws SQLException {
    ResultSetMetaData resultSetMetaData = rs.getMetaData();
    int columnCount = resultSetMetaData.getColumnCount();
    rs.first();
    do{
      StringBuffer row = new StringBuffer();
      for(int idx=1; idx<=columnCount; idx++){
        if(null!=callback){
          try{
            callback.Callback(rs,idx,row);
          }catch (SQLException se){
            throw se;
          }
          catch(Exception e){
            throw new SQLException("在callback中处理结果集时出错了："+e.getMessage(),e);
          }
        }else{
          String cellString = rs.getString(idx);
          row.append(cellString+", ");//故意放到最后面，用于清晰的标注出上一列的结尾。顾法华，2020年5月18日 11点28分
        }
      }
    }while(rs.next());

    rs.close();
  }
}

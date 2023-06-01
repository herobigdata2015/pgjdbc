package org.herodbx.util;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author 顾法华
 * @since 2020年5月25日
 */
public class HeroJdbcTestTool {

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
    try {
      executeSqlcore(url,sqlList,callback);
    }
    catch (SQLException e){
      logger.log(Level.SEVERE,"捕获异常1：",e);
      throw e;
    }
    catch (ClassNotFoundException e){
      logger.log(Level.SEVERE,"捕获异常2：",e);
      throw e;
    }
  }

  static void executeSqlcore(String url, List<String> sqlList, Callback4TestTool callback) throws ClassNotFoundException, SQLException {
//    Class.forName("org.herodbsql.Driver");
    logger.info("BEGIN TO LOAD org.herodbsql.Driver");
    Connection conn = DriverManager.getConnection(url);
    logger.info("LOAD org.herodbsql.Driver OK");
    Statement st = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);

    for(String sql:sqlList){
      logger.info("----------------------------------");
      logger.info("sql:\""+sql+"\"");
      boolean b = st.execute(sql);
      logger.info(String.format("执行结果：第一个结果是个ResultSet？%s",b));
      if (b){
        logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>");
        do{//返回的第一个结果是一个 ResultSet
          ResultSet rs = st.getResultSet();
          displayResultSet(rs,callback);
        }while(st.getMoreResults());
        logger.info("<<<<<<<<<<<<<<<<<<<<<<<<");
      }
    }
    st.close();
    conn.close();
  }

  static private void displayResultSet(ResultSet rs, Callback4TestTool callback) throws SQLException {
    if (rs.last()){
      int rowCount = rs.getRow();
      logger.info(String.format("总行数：%d",rowCount));
    }
    ResultSetMetaData resultSetMetaData = rs.getMetaData();
    int columnCount = resultSetMetaData.getColumnCount();
    logger.info(String.format("总列数：%d",columnCount));
    String header = "";
    for(int idx=1; idx<=columnCount; idx++){
      String columnLabel = resultSetMetaData.getColumnLabel(idx);
      header += columnLabel+", ";
    }
    logger.info(header);

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
            logger.log(Level.WARNING,"在callback中处理结果集时出错了："+e.getMessage(),e);
          }
        }else{
          String cellString = rs.getString(idx);
          row.append(cellString+", ");//故意放到最后面，用于清晰的标注出上一列的结尾。顾法华，2020年5月18日 11点28分
        }
      }
      logger.info(row.toString());
    }while(rs.next());

    rs.close();
  }
}

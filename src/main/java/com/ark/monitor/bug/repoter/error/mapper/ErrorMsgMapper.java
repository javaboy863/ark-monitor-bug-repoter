package com.ark.monitor.bug.repoter.error.mapper;

import com.ark.monitor.bug.repoter.error.dto.ErrorDto;
import com.ark.monitor.bug.repoter.error.entity.ErrorMsg;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 */
public interface ErrorMsgMapper extends BaseMapper<ErrorMsg> {

    @Select("<script>${sql}</script>")
    public List<ErrorDto> execSql(@Param("sql") String sql);

    @Select("<script>${sql}</script>")
    public List<Map> execSql_Simple(@Param("sql") String sql);

    @MapKey("error_type")
    List<Map> errorList(@Param("map") Map map);
}

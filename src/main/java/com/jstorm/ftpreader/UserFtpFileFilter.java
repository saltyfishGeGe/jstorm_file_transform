package com.jstorm.ftpreader;

import com.jstorm.utils.CacheUtils;
import com.jstorm.utils.LoggerBuilder;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Pattern;


public class UserFtpFileFilter implements FTPFileFilter {

	private static final Logger LOG = LoggerBuilder.getLogger(UserFtpFileFilter.class);
	
	private List<String> ls;

	private long lastModifyTime;
	
	public UserFtpFileFilter(List<String> ls, long lastModifyTime) {
		this.ls = ls;
		this.lastModifyTime = lastModifyTime;
	}
	
	@Override
	public boolean accept(FTPFile file) {
		if(ls == null || ls.size() == 0){
			return true;
		}
		String tmpName = file.getName();

		if(ls != null && ls.size() > 0){//代表用户有指定过滤文件名
			for(String s : ls){
				if(s.equals("*") && file.getTimestamp().getTime().getTime() > lastModifyTime){
					return true;
				}
				boolean isMatch = Pattern.compile(s).matcher(tmpName).find();
				// 这里修改成按文件名过滤掉对应的文件
				if(isMatch || file.getTimestamp().getTime().getTime() < lastModifyTime){
					return false;
				}
			}
		}
		return true;
	}
	

}

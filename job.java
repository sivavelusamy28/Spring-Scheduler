package com.example.batchapp.batch.reader;

import com.example.batchapp.model.processTrackerDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Component
public class processTrackerRowMapper implements RowMapper<processTrackerDTO> {
    
    private static final Logger logger = LoggerFactory.getLogger(processTrackerRowMapper.class);
    
    @Override
    public processTrackerDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        
        processTrackerDTO dto = new processTrackerDTO();
        
        dto.setAccountNbr(rs.getString("account_nbr"));
        dto.setLastProcessedTransactionTs(toLocalDateTime(rs.getTimestamp("last_processed_transaction_ts")));
        dto.setProcessTs(toLocalDateTime(rs.getTimestamp("process_ts")));
        dto.settimestamp1Ts(toLocalDateTime(rs.getTimestamp("timestamp1_ts")));
        
        if (rowNum % 1000 == 0) {
            logger.debug("Read row #{}: {} with timestamp1_ts: {}", 
                       rowNum, dto.getAccountNbr(), dto.gettimestamp1Ts());
        }
        
        return dto;
    }
    
    /**
     * Helper method to safely convert Timestamp to LocalDateTime
     */
    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }
}

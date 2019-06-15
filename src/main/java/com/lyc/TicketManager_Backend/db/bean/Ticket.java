package com.lyc.TicketManager_Backend.db.bean;

import com.lyc.TicketManager_Backend.bean.PayMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Ticket {
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private final Set<Seat> seats = new HashSet<>();
    @Id
    private String id;
    @ManyToOne()
    @JoinColumn(name = "movie_show_id", referencedColumnName = "id")
    private MovieShow movieShow;
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
    @Column(name = "pay_method", nullable = false)
    @Convert(converter = PayMethodConverter.class)
    private PayMethod payMethod;
    private Timestamp createTime;
    // 单位：分
    private int price;
    private boolean refund;
    private Timestamp refundTime;

    public static Ticket generateTicket(User user, Set<Seat> seats, MovieShow movieShow, PayMethod payMethod, int price) {
        String id = Long.toHexString(Calendar.getInstance().hashCode())
                + Long.toHexString(user.getId()) + Integer.toHexString(Objects.hash(payMethod.name(), price))
                + seats.hashCode();
        Ticket result = new Ticket(id, movieShow, user, payMethod, new Timestamp(System.currentTimeMillis()), price, false, null);
        result.seats.addAll(seats);
        return result;
    }

    private static class PayMethodConverter implements AttributeConverter<PayMethod, Integer> {

        @Override
        public Integer convertToDatabaseColumn(PayMethod attribute) {
            return attribute.ordinal();
        }

        @Override
        public PayMethod convertToEntityAttribute(Integer dbData) {
            return PayMethod.values()[dbData];
        }
    }
}

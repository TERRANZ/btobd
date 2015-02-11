package ru.terra.btobd.server.entity;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;

/**
 * Date: 17.11.14
 * Time: 2:45
 */
@Entity
@Table(name = "obdinfo", catalog = "obd", schema = "", uniqueConstraints = {@UniqueConstraint(columnNames = {"id"})})
@XmlRootElement
@NamedQueries({@NamedQuery(name = "OBDInfo.findAll", query = "SELECT t FROM OBDInfo t"),
        @NamedQuery(name = "OBDInfo.findById", query = "SELECT t FROM OBDInfo t WHERE t.id = :id"),
        @NamedQuery(name = "OBDInfo.findByUser", query = "SELECT t FROM OBDInfo t WHERE t.userId = :uid"),
        @NamedQuery(name = "OBDInfo.findParameterValues", query = "SELECT t FROM OBDInfo t WHERE t.userId = :uid and t.command = :command"),
        @NamedQuery(name = "OBDInfo.findCurrent", query = "SELECT t FROM OBDInfo t WHERE t.userId = :uid and t.id = (SELECT MAX(u.id) from OBDInfo u)"),
        @NamedQuery(name = "OBDInfo.findParameters", query = "SELECT t FROM OBDInfo t WHERE t.userId = :uid group BY t.command ORDER BY t.command"),
        @NamedQuery(name = "OBDInfo.findByDevice", query = "SELECT t FROM OBDInfo t WHERE t.deviceId = :deviceId")})
public class OBDInfo implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Integer id;
    @Basic(optional = false)
    @Column(name = "deviceid", nullable = false)
    private String deviceId;
    @Basic(optional = false)
    @Column(name = "lat", nullable = false)
    private Double lat;
    @Basic(optional = false)
    @Column(name = "lon", nullable = false)
    private Double lon;
    @Basic(optional = false)
    @Column(name = "command", nullable = false, length = 512)
    private String command;
    @Basic(optional = false)
    @Column(name = "result", nullable = false, length = 512)
    private String result;
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    private User userId;
    @Basic(optional = false)
    @Column(name = "postdate", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date postdate = new Date();

    public OBDInfo() {
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public User getUserId() {
        return userId;
    }

    public void setUserId(User userId) {
        this.userId = userId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Date getPostdate() {
        return postdate;
    }

    public void setPostdate(Date postdate) {
        this.postdate = postdate;
    }

    @Override
    public String toString() {
        return "OBDInfo{" +
                "id=" + id +
                ", deviceId='" + deviceId + '\'' +
                ", lat=" + lat +
                ", lon=" + lon +
                ", command='" + command + '\'' +
                ", result='" + result + '\'' +
                ", userId=" + userId +
                ", postdate=" + postdate +
                '}';
    }
}

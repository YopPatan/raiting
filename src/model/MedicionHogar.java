package model;
// Generated 03-11-2016 11:13:19 by Hibernate Tools 5.2.0.Beta1

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * MedicionHogar generated by hbm2java
 */
@Entity
@Table(name = "medicion_hogar", catalog = "raiting")
public class MedicionHogar implements java.io.Serializable {

	private Integer id;

	private Gse gse;

	private int hogarId;

	private Date fechaInicio;

	private Date fechaTermino;

	private int cable;

	private BigDecimal hogarPeso;

	private Set<MedicionIndividuo> medicionIndividuos = new HashSet<MedicionIndividuo>(0);

	public MedicionHogar() {
	}

	public MedicionHogar(Gse gse, int hogarId, Date fechaInicio, Date fechaTermino, int cable, BigDecimal hogarPeso) {
		this.gse = gse;
		this.hogarId = hogarId;
		this.fechaInicio = fechaInicio;
		this.fechaTermino = fechaTermino;
		this.cable = cable;
		this.hogarPeso = hogarPeso;
	}

	public MedicionHogar(Gse gse, int hogarId, Date fechaInicio, Date fechaTermino, int cable, BigDecimal hogarPeso, 
			Set<MedicionIndividuo> medicionIndividuos) {
		this.gse = gse;
		this.hogarId = hogarId;
		this.fechaInicio = fechaInicio;
		this.fechaTermino = fechaTermino;
		this.cable = cable;
		this.hogarPeso = hogarPeso;
		this.medicionIndividuos = medicionIndividuos;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)

	@Column(name = "id", unique = true, nullable = false)
	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "gse_id", nullable = true)
	public Gse getGse() {
		return this.gse;
	}

	public void setGse(Gse gse) {
		this.gse = gse;
	}

	@Column(name = "hogar_id", nullable = false)
	public int getHogarId() {
		return this.hogarId;
	}

	public void setHogarId(int hogarId) {
		this.hogarId = hogarId;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "fecha_inicio", nullable = false, length = 0)
	public Date getFechaInicio() {
		return this.fechaInicio;
	}

	public void setFechaInicio(Date fechaInicio) {
		this.fechaInicio = fechaInicio;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "fecha_termino", nullable = false, length = 0)
	public Date getFechaTermino() {
		return this.fechaTermino;
	}

	public void setFechaTermino(Date fechaTermino) {
		this.fechaTermino = fechaTermino;
	}

	@Column(name = "cable", nullable = true)
	public int getCable() {
		return this.cable;
	}

	public void setCable(int cable) {
		this.cable = cable;
	}

	@Column(name = "hogar_peso", nullable = true, precision = 10, scale = 2)
	public BigDecimal getHogarPeso() {
		return this.hogarPeso;
	}

	public void setHogarPeso(BigDecimal hogarPeso) {
		this.hogarPeso = hogarPeso;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "medicionHogar")
	public Set<MedicionIndividuo> getMedicionIndividuos() {
		return this.medicionIndividuos;
	}

	public void setMedicionIndividuos(Set<MedicionIndividuo> medicionIndividuos) {
		this.medicionIndividuos = medicionIndividuos;
	}

}

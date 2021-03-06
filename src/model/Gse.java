package model;
// Generated 03-11-2016 11:13:19 by Hibernate Tools 5.2.0.Beta1

import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Gse generated by hbm2java
 */
@Entity
@Table(name = "gse", catalog = "raiting")
public class Gse implements java.io.Serializable {

	private int id;

	private String nombre;

	private Set<MedicionHogar> medicionHogars = new HashSet<MedicionHogar>(0);

	private Set<MedicionIndividuo> medicionIndividuos = new HashSet<MedicionIndividuo>(0);

	public Gse() {
	}

	public Gse(int id, String nombre) {
		this.id = id;
		this.nombre = nombre;
	}

	public Gse(int id, String nombre, Set<MedicionHogar> medicionHogars, Set<MedicionIndividuo> medicionIndividuos) {
		this.id = id;
		this.nombre = nombre;
		this.medicionHogars = medicionHogars;
		this.medicionIndividuos = medicionIndividuos;
	}

	@Id

	@Column(name = "id", unique = true, nullable = false)
	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Column(name = "nombre", nullable = false)
	public String getNombre() {
		return this.nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "gse")
	public Set<MedicionHogar> getMedicionHogars() {
		return this.medicionHogars;
	}

	public void setMedicionHogars(Set<MedicionHogar> medicionHogars) {
		this.medicionHogars = medicionHogars;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "gse")
	public Set<MedicionIndividuo> getMedicionIndividuos() {
		return this.medicionIndividuos;
	}

	public void setMedicionIndividuos(Set<MedicionIndividuo> medicionIndividuos) {
		this.medicionIndividuos = medicionIndividuos;
	}

}

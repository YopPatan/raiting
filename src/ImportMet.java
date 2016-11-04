import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import model.Canal;
import model.Edad;
import model.Gse;
import model.MedicionCanal;
import model.MedicionHogar;
import model.MedicionIndividuo;

public class ImportMet {
	
	public static void main(String[] args) {
		Path file = Paths.get("20161016.Met");
		
		// Inicio conexion a la base de datos usando Hibernate
		SessionFactory factory = new Configuration().configure().buildSessionFactory();
		Session session = factory.openSession();
		
		// Inicio una transaccion SQL
		Transaction tx = session.beginTransaction();
/*		Canal canal = new Canal();
		canal.setNombre("TEST");
		Integer canalPk = (Integer) session.save(canal);
		System.out.println("CANAL PK: " + canalPk);*/
		
		
		try {
			
			// Leo archivo con datos
			List<String> lines = Files.readAllLines(file);

			
			boolean detailData = false;
			Integer medicionHogarId = null;
			Integer medicionIndividuoId = null;
			Calendar fechaInicio = Calendar.getInstance();
			Calendar fechaTermino = Calendar.getInstance();
						
			for (String line: lines) {
//				System.out.println(line);
				String hogarId;
				MedicionHogar hogar = null;
				MedicionIndividuo individuo = null;
				
				if (line.length() == 0 || line.charAt(0) == '<') {
					continue;
				}
				
				char token = line.charAt(0);
				
				switch (token) {
					case 'I':
						detailData = false;
						hogarId = line.substring(1, 9);
						String inicioStr = line.substring(9, 21);
						String terminoStr = line.substring(21, 33);
//						System.out.println("\n INICIO HOGAR\n" + hogarId + " " + fechaInicio + " " + fechaTermino);
						fechaInicio.set(
								Integer.valueOf(inicioStr.substring(0, 4)), 
								Integer.valueOf(inicioStr.substring(4, 6)) - 1, 
								Integer.valueOf(inicioStr.substring(6, 8)),
								Integer.valueOf(inicioStr.substring(8, 10)),
								Integer.valueOf(inicioStr.substring(10)), 
								0);

						fechaTermino.set(
								Integer.valueOf(terminoStr.substring(0, 4)),
								Integer.valueOf(terminoStr.substring(4, 6)) - 1,
								Integer.valueOf(terminoStr.substring(6, 8)),
								Integer.valueOf(terminoStr.substring(8, 10)),
								Integer.valueOf(terminoStr.substring(10)),
								0);
						
						hogar = new MedicionHogar();
						hogar.setHogarId(Integer.valueOf(hogarId));
						hogar.setFechaInicio(fechaInicio.getTime());
						hogar.setFechaTermino(fechaTermino.getTime());
						medicionHogarId = (Integer) session.save(hogar);
						
//						System.out.println(medicionHogarId);
						
						break;
					case 'D':
						String[] ids = line.split(",");
						
//						System.out.println(line);
						
						if (detailData) {
							if (ids[1].equals("0")) {
								break;
							}
							
							Gse gse = session.get(Gse.class, Integer.valueOf(ids[1]));
							Edad edad = session.get(Edad.class, Integer.valueOf(ids[6]));
							
							individuo = session.get(MedicionIndividuo.class, medicionIndividuoId);
							individuo.setGse(gse);
							individuo.setEdad(edad);
							individuo.setCable(Integer.valueOf(ids[2]));
							individuo.setGenero(Integer.valueOf(ids[5]));
							individuo.setEdadCnt(Integer.valueOf(ids[7]));
							individuo.setTipoDuennaCasa(Integer.valueOf(ids[4]));
//							individuo.setTipoTrabaja(Integer.valueOf(ids[4]));
							individuo.setTipoJefeHogar(Integer.valueOf(ids[8]));
							session.update(individuo);

						} else {
							Gse gse = session.get(Gse.class, Integer.valueOf(ids[1]));
							
							hogar = session.get(MedicionHogar.class, medicionHogarId);
							hogar.setGse(gse);
							hogar.setCable(Integer.valueOf(ids[2]));
							session.update(hogar);
//							System.out.println(ids[1] + " " + ids[2]);
						}
						break;
					case 'W':
						String peso = line.substring(1);
						
						if (detailData) {
							individuo = session.get(MedicionIndividuo.class, medicionIndividuoId);
							individuo.setIndividuoPeso(new BigDecimal(peso));
							session.update(individuo);

						} else {
							hogar = session.get(MedicionHogar.class, medicionHogarId);
							hogar.setHogarPeso(new BigDecimal(peso));
							session.update(hogar);
//							System.out.println(peso);
						}
						break;
					case 'Z':
						detailData = true;
						hogarId = line.substring(1, 9);
						String individuoId = line.substring(9);
						
						hogar = session.get(MedicionHogar.class, medicionHogarId);
						
						individuo = new MedicionIndividuo();
						individuo.setMedicionHogar(hogar);
						individuo.setIndividuoId(Integer.valueOf(individuoId));
						medicionIndividuoId = (Integer) session.save(individuo);
						
//						System.out.println(hogarId + " " + individuoId);
						break;
					default:
						String tvId = line.substring(0, 2);
						String canalId = line.substring(2, 11);
						String minVistos = line.substring(12);
						
						Pattern p = Pattern.compile("[A|B][0-9]+");
						Matcher m = p.matcher(minVistos);
						
						Calendar fechaInicioCanal = (Calendar) fechaInicio.clone();
						
						individuo = session.get(MedicionIndividuo.class, medicionIndividuoId);
						
//						System.out.println("\n" + tvId + " " + canalId + " " + minVistos);
						
						while (m.find()) {
							char tipo = m.group().charAt(0);
							String minutos = m.group().substring(1);
							
							if (tipo == 'B') {
								MedicionCanal canal = new MedicionCanal();
								Calendar fechaTerminoCanal = (Calendar) fechaInicioCanal.clone();
								fechaTerminoCanal.add(Calendar.MINUTE, Integer.valueOf(minutos));
								
								canal.setMedicionIndividuo(individuo);
								canal.setTelevisorId(Integer.valueOf(tvId));
								canal.setCanalId(Integer.valueOf(canalId));
								canal.setFechaInicio(fechaInicioCanal.getTime());
								canal.setFechaTermino(fechaTerminoCanal.getTime());
								session.save(canal);
							}
							
							fechaInicioCanal.add(Calendar.MINUTE, Integer.valueOf(minutos));
							
							//System.out.println(tipo + " " + minutos + " " + fechaCanal.getTime());
							
						}
						break;
				}
				
			}
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		tx.commit();
	}
}
import Image from "next/image";
import Link from "next/link";
import styles from "../theme.module.css";
import type { ProjectItemModel } from "@/components/theme/models";

export default function ProjectSection({ projects }: { projects: ProjectItemModel[] }) {
  return (
    <section className={styles.projectArea}>
      <div className={styles.projectViewport}>
        <div className={styles.projectStrip}>
          {projects.map((project) => (
            <article key={project.id} className={styles.projectPanel}>
              <div className={styles.projectItem}>
                <div className={styles.projectThumb}>
                  {project.image ? (
                    <Image
                      src={project.image.src}
                      alt={project.image.alt}
                      fill
                      sizes="(max-width: 991px) 80vw, 38vw"
                      className={styles.coverImage}
                    />
                  ) : null}
                </div>

                <div className={styles.projectContent}>
                  {project.subtitle ? <span>{project.subtitle}</span> : null}
                  <h4 className={styles.projectTitle}>
                    {!project.href ? (
                      project.title
                    ) : project.external ? (
                      <a href={project.href} target="_blank" rel="noopener noreferrer">
                        {project.title}
                      </a>
                    ) : (
                      <Link href={project.href}>{project.title}</Link>
                    )}
                  </h4>
                </div>
              </div>
            </article>
          ))}
        </div>
      </div>
    </section>
  );
}

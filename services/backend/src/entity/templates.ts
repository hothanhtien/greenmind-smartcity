import {Column, CreateDateColumn, Entity, OneToMany, PrimaryColumn, UpdateDateColumn, ManyToOne, JoinColumn} from "typeorm";
import {Questions} from "./questions";
import {Models} from "./models";

export const TEMPLATES_TABLE_NAME = 'templates';

@Entity(TEMPLATES_TABLE_NAME)
export class Template {
    @PrimaryColumn({type:"varchar"})
    id!: string;

    @Column({type:"varchar"})
    name!: string;

    @Column({type: "text", nullable: true})
    description?: string;

    @Column({type: "varchar"})
    intent!: string;

    @Column({type: "text"})
    prompt!: string;

    @Column({type: "json", nullable: true})
    used_placeholders?: string[];

    @Column({type: "varchar", nullable: true})
    question_type?: string;

    @Column({type: "text", nullable: true})
    filled_prompt?: string;

    @Column({type: "varchar", nullable: true})
    trait?: string;

    // Answer fields (merged from template_answers)
    @Column({type: "varchar", nullable: true})
    answer_type?: string;

    @Column({type: "json", nullable: true})
    answer_scale?: number[];

    @Column({type: "json", nullable: true})
    answer_labels?: string[];

    @Column({type: "json", nullable: true})
    answer_options?: string[];

    @ManyToOne(() => Models, {nullable: true})
    @JoinColumn({name: 'model_id'})
    model?: Models;

    @OneToMany(() => Questions, questions => questions.template, {onDelete: "CASCADE"})
    questions!: Questions[];

    @CreateDateColumn({type: 'timestamp'})
    createdAt!: Date;

    @UpdateDateColumn({type: 'timestamp'})
    updatedAt!: Date;
}
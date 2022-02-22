import { Component, OnInit, ViewEncapsulation, Input } from '@angular/core';
import { PostService } from 'src/app/auth/shared/post.service';
import { PostModel } from 'src/app/auth/shared/post-model';
import { faComments } from '@fortawesome/free-solid-svg-icons';
import { Router } from '@angular/router';

@Component({
  selector: 'app-post-tile',
  templateUrl: './post-tile.component.html',
  styleUrls: ['./post-tile.component.css'],
  encapsulation: ViewEncapsulation.None,
})
export class PostTileComponent implements OnInit {
  faComments = faComments;
  @Input() posts: PostModel[] = [];

  constructor(private router: Router) {}

  ngOnInit(): void {}

  goToPost(id: number): void {
    this.router.navigateByUrl('/view-post/' + id);
  }
}
